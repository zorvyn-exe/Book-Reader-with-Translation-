package com.romanurdu.pdfreader.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Library : NavRoutes("library")
    data object Vocabulary : NavRoutes("vocabulary")
    data object DictionarySearch : NavRoutes("dictionary_search")
    data object Reader : NavRoutes("reader/{bookId}") {
        fun createRoute(bookId: Long) = "reader/$bookId"
    }
}
