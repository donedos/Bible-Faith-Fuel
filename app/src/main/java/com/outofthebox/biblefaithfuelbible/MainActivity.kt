package com.outofthebox.biblefaithfuelbible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outofthebox.biblefaithfuelbible.ui.screens.BibleScreen
import com.outofthebox.biblefaithfuelbible.ui.screens.SplashScreen
import com.outofthebox.biblefaithfuelbible.ui.theme.BibleFaithFuelBibleTheme
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable immersive fullscreen (hiding notification bar and navigation buttons)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            val viewModel: BibleViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            BibleFaithFuelBibleTheme(
                accentColor = uiState.selectedIosAccent,
                darkTheme = uiState.isDarkTheme
            ) {
                if (!uiState.hasShownSplash) {
                    SplashScreen(
                        onSplashFinished = { viewModel.markSplashShown() }
                    )
                } else {
                    BibleScreen(viewModel = viewModel)
                }
            }
        }
    }
}
