package com.romanurdu.pdfreader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romanurdu.pdfreader.ui.theme.ReadingTheme

@Composable
fun ReadingSettingsDialog(
    currentTheme: ReadingTheme,
    zoomScale: Float,
    onThemeSelect: (ReadingTheme) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Display & Theme Settings") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reading Theme",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ReadingTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelect(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = theme.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Zoom Scale: ${(zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onZoomOut) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Smaller")
                    }
                    Button(onClick = onZoomIn) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Larger")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
