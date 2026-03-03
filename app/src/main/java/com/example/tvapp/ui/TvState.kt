package com.example.tvapp.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * MVI State for the TV App Content Screens.
 * Marked as @Stable/ @Immutable to ensure the Compose compiler can optimize recompositions,
 * which is critical for smooth performance on TV hardware.
 */
@Immutable
data class TvContentItem(
    val id: String,
    val title: String,
    val imageUrl: String = "",
    val description: String = ""
)

@Immutable
data class TvCategory(
    val id: String,
    val title: String,
    val items: List<TvContentItem>
)

@Stable
data class TvScreenState(
    val title: String = "",
    val subtitle: String = "",
    val categories: List<TvCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val focusedItemId: String? = null
)

/**
 * User Intents for the TV Screen.
 */
sealed class TvIntent {
    data class LoadScreen(val title: String) : TvIntent()
    data class FocusItem(val itemId: String) : TvIntent()
    data class SelectItem(val itemId: String) : TvIntent()
}
