package com.example.tvapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.ui.TvContentScreen
import com.example.tvnav.core.TvNavigationFramework

/**
 * Navigation Wrapper for the app.
 * Plugs the app's specific routes and UI into the generic TvNavigationFramework library.
 */
@Composable
fun TvNavigationWrapper() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TvNavigationFramework(
        items = NavigationRoutes.allRoutes,
        currentItemId = currentRoute,
        homeId = NavigationRoutes.Home.id,
        onItemSelected = { item ->
            val route = (item as NavigationRoutes).route
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { contentModifier ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.Home.route,
            modifier = contentModifier.fillMaxSize()
        ) {
            composable(NavigationRoutes.Home.route) {
                TvContentScreen(title = "Home")
            }
            composable(NavigationRoutes.Movies.route) {
                TvContentScreen(title = "Movies")
            }
            composable(NavigationRoutes.Shows.route) {
                TvContentScreen(title = "TV Shows")
            }
            composable(NavigationRoutes.Search.route) {
                TvContentScreen(title = "Search")
            }
            composable(NavigationRoutes.Settings.route) {
                TvContentScreen(title = "Settings")
            }
        }
    }
}
