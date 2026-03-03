package com.example.tvapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tvnav.core.TvNavItem

/**
 * Sealed class defining the routes for the TV application.
 * Implements the library's TvNavItem to integrate with the Navigation Framework.
 */
sealed class NavigationRoutes(
    override val id: String,
    override val label: String,
    override val icon: ImageVector
) : TvNavItem {
    val route: String get() = id
    object Home : NavigationRoutes("home", "Home", Icons.Default.Home)
    object Movies : NavigationRoutes("movies", "Movies", Icons.Default.Movie)
    object Shows : NavigationRoutes("shows", "TV Shows", Icons.Default.Tv)
    object Search : NavigationRoutes("search", "Search", Icons.Default.Search)
    object Settings : NavigationRoutes("settings", "Settings", Icons.Default.Settings)

    companion object {
        val allRoutes = listOf(Home, Movies, Shows, Search, Settings)
    }
}
