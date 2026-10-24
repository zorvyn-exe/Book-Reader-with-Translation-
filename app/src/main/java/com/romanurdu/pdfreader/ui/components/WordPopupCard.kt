package com.romanurdu.pdfreader.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.romanurdu.pdfreader.data.model.LookupSource
import com.romanurdu.pdfreader.data.model.WordLookupResult
import com.romanurdu.pdfreader.ui.viewmodel.TapCoordinates
import kotlin.math.roundToInt

@Composable
fun WordPopupCard(
    word: String,
    result: WordLookupResult?,
    coords: TapCoordinates?,
    isLoading: Boolean,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onSpeak: () -> Unit,
    onOnlineLookup: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (coords == null) return

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // Convert CSS coordinates from WebView to Android screen pixels
    // CSS coordinates are multiplied by devicePixelRatio
    val wordLeftPx = coords.left * coords.dpr
    val wordRightPx = coords.right * coords.dpr
    val wordTopPx = coords.top * coords.dpr
    val wordBottomPx = coords.bottom * coords.dpr

    val wordCenterXPx = (wordLeftPx + wordRightPx) / 2f

    // Desired popup width
    val popupWidthDp = 300.dp
    val popupWidthPx = with(density) { popupWidthDp.toPx() }
    val estimatedHeightPx = with(density) { 150.dp.toPx() }

    // Clamp horizontal position so card stays entirely on screen
    val halfWidth = popupWidthPx / 2f
    val clampedXPx = wordCenterXPx.coerceIn(halfWidth + 24f, screenWidthPx - halfWidth - 24f) - halfWidth

    // Decide if popup should anchor ABOVE or BELOW the tapped word
    // If word is near top (< 160dp), anchor BELOW word. Otherwise anchor ABOVE word.
    val marginPx = with(density) { 12.dp.toPx() }
    val isNearTop = wordTopPx < (estimatedHeightPx + marginPx + with(density) { 56.dp.toPx() })

    val clampedYPx = if (isNearTop) {
        wordBottomPx + marginPx
    } else {
        (wordTopPx - estimatedHeightPx - marginPx).coerceAtLeast(with(density) { 56.dp.toPx() })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .offset { IntOffset(clampedXPx.roundToInt(), clampedYPx.roundToInt()) }
                .width(popupWidthDp)
                .wrapContentHeight()
                .shadow(10.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header: Word + Speaker + Save + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = word,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Save to Vocabulary button
                        IconButton(
                            onClick = onSaveToggle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Saved" else "Save Word",
                                tint = if (isSaved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (result != null) {
                    // Roman Urdu Meaning Display
                    Text(
                        text = result.matchedMeaning,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Context Note / POS indicator
                    if (!result.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.note,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Online fallback button when word is missing offline
                    if (!result.isFound && result.source == LookupSource.NOT_FOUND) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOnlineLookup,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Online Lookup", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
