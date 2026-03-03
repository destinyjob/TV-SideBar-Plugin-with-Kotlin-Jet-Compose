package com.example.tvnav.core

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface defines the contract for any navigation item in the framework.
 * This ensures the framework remains agnostic to specific app routes.
 */
interface TvNavItem {
    val id: String
    val label: String
    val icon: ImageVector
}
