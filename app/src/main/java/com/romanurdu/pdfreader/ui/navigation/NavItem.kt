package com.romanurdu.pdfreader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Library : NavItem(
        route = NavRoutes.Library.route,
        title = "Library",
        icon = Icons.AutoMirrored.Filled.MenuBook
    )

    data object Vocabulary : NavItem(
        route = NavRoutes.Vocabulary.route,
        title = "Vocabulary",
        icon = Icons.Default.Bookmark
    )

    data object Dictionary : NavItem(
        route = NavRoutes.DictionarySearch.route,
        title = "Dictionary",
        icon = Icons.Default.Search
    )
}
