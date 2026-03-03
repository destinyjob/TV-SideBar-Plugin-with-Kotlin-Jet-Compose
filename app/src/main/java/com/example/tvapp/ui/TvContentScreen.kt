package com.example.tvapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tvapp.ui.components.TvPosterCard

/**
 * Refactored Content Screen.
 * 
 * Reverted to simple focus logic as confirmed by user:
 * - DOWN movement resets focus to the 1st item (Index 0).
 * - UP movement is allowed to use default behavior for position preservation.
 */
@OptIn(ExperimentalTvMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TvContentScreen(
    title: String,
    viewModel: TvViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val firstItemFocusRequester = remember { FocusRequester() }
    
    // Throttling state to manage scroll speed
    var lastScrollTime by remember { mutableStateOf(0L) }
    val throttleMs = 220L // Stabilized sweet spot for premium feel

    val backgroundColor = when (title.lowercase()) {
        "home" -> Color(0xFF141414)
        "movies" -> Color(0xFF1A1A2E)
        "tv shows" -> Color(0xFF16213E)
        "search" -> Color(0xFF0F3460)
        "settings" -> Color(0xFF1B1B1B)
        else -> Color(0xFF141414)
    }

    val view = LocalView.current // Pre-capture for use in event lambdas

    // Initialize content load via Intent
    LaunchedEffect(title) {
        viewModel.onIntent(TvIntent.LoadScreen(title))
    }

    LaunchedEffect(uiState.categories) {
        if (uiState.categories.isNotEmpty()) {
            // Robust focus retry loop to handle lazy composition delays
            var success = false
            var attempts = 0
            while (!success && attempts < 10) {
                try {
                    // Wait for composition and attachment
                    delay(if (attempts == 0) 300 else 200)
                    firstItemFocusRequester.requestFocus()
                    success = true
                } catch (e: Exception) {
                    attempts++
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val isNavigationKey = when (event.key) {
                        Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight -> true
                        else -> false
                    }
                    if (isNavigationKey) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScrollTime < throttleMs) {
                            true
                        } else {
                            lastScrollTime = currentTime
                            // Trigger system sound for throttled navigation using captured view
                            view.playSoundEffect(
                                when (event.key) {
                                    Key.DirectionUp -> SoundEffectConstants.NAVIGATION_UP
                                    Key.DirectionDown -> SoundEffectConstants.NAVIGATION_DOWN
                                    Key.DirectionLeft -> SoundEffectConstants.NAVIGATION_LEFT
                                    Key.DirectionRight -> SoundEffectConstants.NAVIGATION_RIGHT
                                    else -> SoundEffectConstants.NAVIGATION_DOWN
                                }
                            )
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 27.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item(key = "header") {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = uiState.title.ifEmpty { title },
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = uiState.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        itemsIndexed(uiState.categories, key = { _, category -> category.id }) { index, category ->
            ContentRow(
                category = category,
                initialFocusRequester = if (index == 0) firstItemFocusRequester else null,
                isRoundStyle = title.lowercase() == "settings",
                onItemFocused = { itemId -> viewModel.onIntent(TvIntent.FocusItem(itemId)) },
                onItemClicked = { itemId -> viewModel.onIntent(TvIntent.SelectItem(itemId)) }
            )
        }
    }
}
@OptIn(ExperimentalTvMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun ContentRow(
    category: TvCategory,
    initialFocusRequester: FocusRequester? = null,
    isRoundStyle: Boolean = false,
    onItemFocused: (String) -> Unit = {},
    onItemClicked: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val firstItemRequester = remember { FocusRequester() }
    val view = LocalView.current // Pre-capture for use in focus properties

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(end = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isRoundStyle) 120.dp else 180.dp)
                .focusGroup()
                .focusRestorer()
                .focusProperties { 
                    enter = { direction -> 
                        if (direction == FocusDirection.Down) {
                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                            val isIndex0Visible = visibleItems.any { it.index == 0 }
                            
                            // Trigger system sound for programmatic vertical transition using captured view
                            view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)

                            if (isIndex0Visible) {
                                firstItemRequester
                            } else {
                                scope.launch {
                                    listState.scrollToItem(0)
                                    firstItemRequester.requestFocus()
                                }
                                FocusRequester.Cancel
                            }
                        } else {
                            FocusRequester.Default
                        }
                    }
                }
        ) {
            itemsIndexed(category.items, key = { _, item -> item.id }) { index, item ->
                val cardModifier = if (index == 0) {
                    val m = Modifier.focusRequester(firstItemRequester)
                    if (initialFocusRequester != null) m.focusRequester(initialFocusRequester) else m
                } else {
                    Modifier
                }

                TvPosterCard(
                    label = if (isRoundStyle) "${category.title[0]}" else item.title,
                    index = index,
                    imageUrl = item.imageUrl,
                    aspectRatio = if (isRoundStyle) 1f else 16f / 9f,
                    shape = if (isRoundStyle) CircleShape else RoundedCornerShape(8.dp),
                    modifier = cardModifier,
                    onFocus = { onItemFocused(item.id) },
                    onClick = { onItemClicked(item.id) }
                )
            }
        }
    }
}
