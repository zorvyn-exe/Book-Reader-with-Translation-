package com.romanurdu.pdfreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.romanurdu.pdfreader.R

@Composable
fun OcrWarningBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.scanned_pdf_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                Text(
                    text = "Scanned images detected without selectable text. Tap for OCR info.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }

            TextButton(onClick = { showHelpDialog = true }) {
                Text("How to Fix", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("What is an OCR PDF?") },
            text = {
                Column {
                    Text(
                        "Scanned books are photographs of book pages. To detect words when tapping, the PDF must have a digital text layer.\n\n" +
                                "How to enable Roman Urdu word tapping on scanned PDFs:\n" +
                                "1. Open Google Drive on your computer or phone and upload the PDF.\n" +
                                "2. Right-click and choose 'Open with Google Docs' (this automatically OCRs the text).\n" +
                                "3. Or use free tools like OCRmyPDF, CamScanner, or Adobe Acrobat to create a searchable PDF.\n" +
                                "4. Re-import the searchable PDF into this reader!"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
