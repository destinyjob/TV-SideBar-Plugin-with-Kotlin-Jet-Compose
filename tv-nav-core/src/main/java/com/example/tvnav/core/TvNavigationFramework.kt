package com.example.tvnav.core

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A generic, content-agnostic TV Navigation Framework.
 * Implements cinematic standards for focus management and spatial navigation.
 * 
 * @param items List of navigation items to display in the sidebar.
 * @param currentItemId The ID of the currently selected item.
 * @param onItemSelected Callback when a navigation item is selected (via click or right-Dpad).
 * @param homeId The ID of the "Home" or "Root" item for backtracking logic.
 * @param content The main screen content, receiving a modifier with focus properties.
 */
@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TvNavigationFramework(
    items: List<TvNavItem>,
    currentItemId: String?,
    onItemSelected: (TvNavItem) -> Unit,
    modifier: Modifier = Modifier,
    homeId: String? = null,
    content: @Composable (contentModifier: Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Cinematic Backtracking: 
    // If closed -> Open Sidebar. 
    // If open and not on Home -> Go to Home.
    // If on Home or no homeId -> Let default behavior handle it (likely exit).
    BackHandler(enabled = true) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            scope.launch { drawerState.setValue(DrawerValue.Open) }
        } else {
            if (homeId != null && currentItemId != homeId) {
                items.find { it.id == homeId }?.let { onItemSelected(it) }
            } else {
                // If already open and on home, or no homeId, we try to close first or let it pass
                scope.launch { drawerState.setValue(DrawerValue.Closed) }
            }
        }
    }
    
    val contentFocusRequester = remember { FocusRequester() }
    val sidebarAnchorFocusRequester = remember { FocusRequester() }
    var isNavigating by remember { mutableStateOf(false) }

    // Requesters for each sidebar item
    val sidebarFocusRequesters = remember(items) {
        items.associate { it.id to FocusRequester() }
    }

    val view = LocalView.current

    val handleSelection: (TvNavItem) -> Unit = { item ->
        if (!isNavigating) {
            isNavigating = true
            scope.launch {
                onItemSelected(item)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                drawerState.setValue(DrawerValue.Closed)
                
                // Robust focus handover to content
                var focused = false
                var attempts = 0
                while (!focused && attempts < 10) {
                    delay(100)
                    try {
                        contentFocusRequester.requestFocus()
                        focused = true
                    } catch (e: Exception) {
                        attempts++
                    }
                }
                isNavigating = false
            }
        }
    }

    NavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
                    .focusRequester(sidebarAnchorFocusRequester)
                    .focusRestorer { 
                        sidebarFocusRequesters[currentItemId] ?: FocusRequester.Default 
                    }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = currentItemId == item.id
                    
                    NavigationDrawerItem(
                        selected = isSelected,
                        onClick = { handleSelection(item) },
                        modifier = Modifier
                            .focusRequester(sidebarFocusRequesters[item.id]!!)
                            .onPreviewKeyEvent {
                                if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionRight) {
                                    view.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT)
                                    handleSelection(item)
                                    true
                                } else {
                                    false
                                }
                            },
                        leadingContent = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp)
                .focusProperties {
                    exit = { direction ->
                        if (direction == FocusDirection.Left) {
                            sidebarAnchorFocusRequester
                        } else {
                            FocusRequester.Default
                        }
                    }
                }
                .focusGroup()
                .focusRequester(contentFocusRequester)
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}
