package com.romanurdu.pdfreader.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.romanurdu.pdfreader.ui.components.OcrWarningBanner
import com.romanurdu.pdfreader.ui.components.PdfViewerWebView
import com.romanurdu.pdfreader.ui.components.ReadingSettingsDialog
import com.romanurdu.pdfreader.ui.components.WordPopupCard
import com.romanurdu.pdfreader.ui.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val book by viewModel.currentBook.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()
    val tapCoords by viewModel.tapCoordinates.collectAsState()
    val lookupResult by viewModel.lookupResult.collectAsState()
    val isLoadingMeaning by viewModel.isLoadingMeaning.collectAsState()
    val isSavedToVocab by viewModel.isSavedToVocab.collectAsState()
    val isScannedPdf by viewModel.isScannedPdf.collectAsState()
    val readingTheme by viewModel.readingTheme.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book?.title ?: "PDF Reader",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "Page $currentPage of $totalPages",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Bookmark Page
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Theme & Display Settings
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (book != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Scanned PDF OCR warning banner (Feature 13)
                    if (isScannedPdf) {
                        OcrWarningBanner(
                            onDismiss = { viewModel.dismissScannedWarning() }
                        )
                    }

                    // PDF Viewer Web Container
                    PdfViewerWebView(
                        pdfFilePath = book!!.localFilePath,
                        theme = readingTheme,
                        zoomScale = zoomScale,
                        targetPage = book!!.currentPage,
                        onWordTapped = { word, sentence, coords ->
                            viewModel.onWordTapped(word, sentence, coords)
                        },
                        onPageChanged = { page, total, scroll ->
                            viewModel.onPageChanged(page, total, scroll)
                        },
                        onScannedPdfDetected = { pageNum ->
                            viewModel.onScannedPdfDetected(pageNum)
                        },
                        onDocumentLoaded = { total ->
                            viewModel.onDocumentLoaded(total)
                        },
                        onDocumentError = {
                            // Handled in viewer
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Word Popup Anchored immediately above selected word
            AnimatedVisibility(
                visible = selectedWord != null,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f)
            ) {
                // Dim background area outside popup so tap outside closes popup cleanly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.clearSelectedWord()
                        }
                ) {
                    selectedWord?.let { word ->
                        WordPopupCard(
                            word = word,
                            result = lookupResult,
                            coords = tapCoords,
                            isLoading = isLoadingMeaning,
                            isSaved = isSavedToVocab,
                            onSaveToggle = { viewModel.saveCurrentWordToVocab() },
                            onSpeak = { viewModel.speakCurrentWord() },
                            onOnlineLookup = { viewModel.performOnlineLookup() },
                            onDismiss = { viewModel.clearSelectedWord() }
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        ReadingSettingsDialog(
            currentTheme = readingTheme,
            zoomScale = zoomScale,
            onThemeSelect = { viewModel.setReadingTheme(it) },
            onZoomIn = { viewModel.zoomIn() },
            onZoomOut = { viewModel.zoomOut() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
