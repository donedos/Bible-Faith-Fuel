package com.outofthebox.biblefaithfuelbible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outofthebox.biblefaithfuelbible.ui.screens.BibleScreen
import com.outofthebox.biblefaithfuelbible.ui.theme.BibleFaithFuelBibleTheme
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BibleViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            BibleFaithFuelBibleTheme(
                accentColor = uiState.selectedIosAccent,
                darkTheme = uiState.isDarkTheme
            ) {
                BibleScreen(viewModel = viewModel)
            }
        }
    }
}
