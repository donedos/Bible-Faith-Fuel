package com.outofthebox.biblefaithfuelbible

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.MobileAds
import com.outofthebox.biblefaithfuelbible.ui.screens.BibleScreen
import com.outofthebox.biblefaithfuelbible.ui.screens.SplashScreen
import com.outofthebox.biblefaithfuelbible.ui.theme.BibleFaithFuelBibleTheme
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel
import com.outofthebox.biblefaithfuelbible.widget.WidgetUpdater

class MainActivity : ComponentActivity() {
    private var pendingVerseRef by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(this)

        // Enable immersive fullscreen (hiding notification bar and navigation buttons)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        pendingVerseRef = intent?.getStringExtra(WidgetUpdater.EXTRA_VERSE_REF)
        intent?.removeExtra(WidgetUpdater.EXTRA_VERSE_REF)

        setContent {
            val viewModel: BibleViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(pendingVerseRef, uiState.hasShownSplash) {
                val ref = pendingVerseRef
                if (!ref.isNullOrBlank() && uiState.hasShownSplash) {
                    viewModel.navigateToVerse(ref)
                    pendingVerseRef = null
                }
            }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val verseRef = intent.getStringExtra(WidgetUpdater.EXTRA_VERSE_REF)
        if (!verseRef.isNullOrBlank()) {
            pendingVerseRef = verseRef
        }
        intent.removeExtra(WidgetUpdater.EXTRA_VERSE_REF)
    }
}
