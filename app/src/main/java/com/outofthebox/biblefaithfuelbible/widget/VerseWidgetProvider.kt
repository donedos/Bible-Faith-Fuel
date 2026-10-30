package com.outofthebox.biblefaithfuelbible.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class VerseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAsync(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        updateAsync(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdater.ACTION_REFRESH) {
            WidgetUpdater.clearRefreshTimestamp(context)
            updateAsync(context)
        }
    }

    private fun updateAsync(context: Context) {
        val pendingResult = goAsync()
        Thread {
            try {
                WidgetUpdater.updateAllNow(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }
}
