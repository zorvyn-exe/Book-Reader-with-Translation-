package com.romanurdu.pdfreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.romanurdu.pdfreader.ui.navigation.NavItem
import com.romanurdu.pdfreader.ui.navigation.NavRoutes
import com.romanurdu.pdfreader.ui.screens.DictionarySearchScreen
import com.romanurdu.pdfreader.ui.screens.LibraryScreen
import com.romanurdu.pdfreader.ui.screens.ReaderScreen
import com.romanurdu.pdfreader.ui.screens.VocabularyScreen
import com.romanurdu.pdfreader.ui.theme.RomanUrduPDFReaderTheme
import com.romanurdu.pdfreader.ui.viewmodel.BookViewModel
import com.romanurdu.pdfreader.ui.viewmodel.ReaderViewModel
import com.romanurdu.pdfreader.ui.viewmodel.VocabularyViewModel

class MainActivity : ComponentActivity() {

    private val bookViewModel: BookViewModel by viewModels()
    private val readerViewModel: ReaderViewModel by viewModels()
    private val vocabularyViewModel: VocabularyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle external intent if user opened a PDF file from an external file manager
        handleIncomingPdfIntent(intent)

        setContent {
            RomanUrduPDFReaderTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide bottom navigation when inside PDF reader for immersive full-screen reading
                val isReaderScreen = currentRoute?.startsWith("reader/") == true

                Scaffold(
                    bottomBar = {
                        if (!isReaderScreen) {
                            NavigationBar {
                                val navItems = listOf(
                                    NavItem.Library,
                                    NavItem.Vocabulary,
                                    NavItem.Dictionary
                                )
                                navItems.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationBarItem(
                                        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        selected = selected,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Library.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.Library.route) {
                            LibraryScreen(
                                viewModel = bookViewModel,
                                onBookClick = { bookId ->
                                    navController.navigate(NavRoutes.Reader.createRoute(bookId))
                                }
                            )
                        }

                        composable(NavRoutes.Vocabulary.route) {
                            VocabularyScreen(
                                viewModel = vocabularyViewModel
                            )
                        }

                        composable(NavRoutes.DictionarySearch.route) {
                            DictionarySearchScreen()
                        }

                        composable(
                            route = NavRoutes.Reader.route,
                            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                            ReaderScreen(
                                bookId = bookId,
                                viewModel = readerViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingPdfIntent(intent)
    }

    private fun handleIncomingPdfIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri: Uri = intent.data!!
            bookViewModel.importPdf(uri) { /* Imported */ }
        }
    }
}
