package com.wngud.timebox.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBoxerTopBar() {
    TopAppBar(
        title = {
            Text(
                "TimeBoxer",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground // Theme Color
            )
        },
        actions = {
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = { /* Calendar */ }) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}