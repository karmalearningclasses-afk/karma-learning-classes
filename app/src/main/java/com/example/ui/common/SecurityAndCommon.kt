package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguagePreference

interface SecureWindowController {
    fun setProtection(isProtected: Boolean)
}

val LocalSecureWindowController = staticCompositionLocalOf<SecureWindowController> {
    object : SecureWindowController {
        override fun setProtection(isProtected: Boolean) {}
    }
}

val LocalLanguageState = compositionLocalOf {
    mutableStateOf(LanguagePreference.BILINGUAL)
}

@Composable
fun ProtectedContentBadge(
    modifier: Modifier = Modifier,
    isProtected: Boolean = true,
    showDetailedBanner: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        color = if (isProtected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .testTag("protected_content_badge")
            .clickable { showDialog = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Protected",
                tint = if (isProtected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isProtected) "Protected Content" else "Public Content",
                style = MaterialTheme.typography.labelSmall,
                color = if (isProtected) MaterialTheme.colorScheme.onPrimaryContainer else Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Content Protection Active")
                }
            },
            text = {
                Text(
                    "This study material is secured using native Android FLAG_SECURE window protection.\n\n" +
                    "• Screen capture and recording are disabled.\n" +
                    "• Prevents unauthorized distribution of paid lectures, premium notes, and mock test answer keys.\n" +
                    "• Settings are managed by your Teacher Admin."
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
fun ChemicalFormulaBadge(formula: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⚡ Equation:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formula,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun LanguageToggleChip(
    current: LanguagePreference,
    onChanged: (LanguagePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            LanguagePreference.BILINGUAL to "Bilingual",
            LanguagePreference.HINDI to "हिन्दी",
            LanguagePreference.ENGLISH to "English"
        ).forEach { (pref, label) ->
            val isSelected = current == pref
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onChanged(pref) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
