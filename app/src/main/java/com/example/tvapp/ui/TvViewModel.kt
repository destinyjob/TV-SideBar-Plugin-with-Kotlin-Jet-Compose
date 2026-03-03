package com.example.tvapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the TV Content Screen implementing the MVI pattern.
 * Ensures a single source of truth and atomic state updates.
 */
class TvViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TvScreenState())
    val uiState: StateFlow<TvScreenState> = _uiState.asStateFlow()

    fun onIntent(intent: TvIntent) {
        when (intent) {
            is TvIntent.LoadScreen -> loadContent(intent.title)
            is TvIntent.FocusItem -> updateFocusedItem(intent.itemId)
            is TvIntent.SelectItem -> handleSelection(intent.itemId)
        }
    }

    private fun loadContent(title: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, title = title) }
            
            // Artificial delay to simulate network/database call
            // In a real app, this would fetch from a Repository
            val categories = when (title.lowercase()) {
                "home" -> listOf(
                    createCategory("Trending Now", "Trending"),
                    createCategory("Continue Watching", "WatchNext"),
                    createCategory("Recommended", "Rec")
                )
                "movies" -> listOf(
                    createCategory("Popular Movies", "PopMovies"),
                    createCategory("Action", "Action")
                )
                "tv shows" -> listOf(
                    createCategory("Binge-worthy", "Binge"),
                    createCategory("Reality TV", "Reality")
                )
                else -> emptyList()
            }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    categories = categories,
                    subtitle = "Browse ${title.lowercase()} categories"
                ) 
            }
        }
    }

    private fun createCategory(title: String, prefixId: String): TvCategory {
        return TvCategory(
            id = prefixId,
            title = title,
            items = (1..12).map { 
                TvContentItem(
                    id = "$prefixId-$it",
                    title = if (prefixId.contains("Movies")) "Movie $it" else "Show $it",
                    imageUrl = "https://picsum.photos/seed/${prefixId}${it}/400/225", // 16:9 ratio
                    description = "Cinematic description for $prefixId item $it"
                )
            }
        )
    }

    private fun updateFocusedItem(itemId: String) {
        _uiState.update { it.copy(focusedItemId = itemId) }
    }

    private fun handleSelection(itemId: String) {
        // Handle item selection (e.g., navigate to detail screen)
        android.util.Log.d("TvViewModel", "Selected item: $itemId")
    }
}
