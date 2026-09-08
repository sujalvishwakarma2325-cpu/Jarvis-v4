package com.jarvis.v4

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jarvis.v4.ai.ModelDownloader
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var downloader: ModelDownloader

    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var details: TextView
    private lateinit var button: Button

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        downloader =
            ModelDownloader(this)

        createScreen()

        if (downloader.isInstalled()) {
            showInstalled()
        } else {
            showDownloadReady()
        }
    }

    private fun createScreen() {

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.setPadding(
            40,
            80,
            40,
            40
        )

        root.setBackgroundColor(
            Color.rgb(5, 8, 12)
        )

        title =
            TextView(this)

        title.text =
            "JARVIS V4"

        title.textSize =
            34f

        title.setTextColor(
            Color.WHITE
        )

        title.gravity =
            Gravity.CENTER

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        subtitle =
            TextView(this)

        subtitle.text =
            "LOCAL AI SETUP"

        subtitle.textSize =
            18f

        subtitle.setTextColor(
            Color.rgb(0, 210, 255)
        )

        subtitle.gravity =
            Gravity.CENTER

        val subtitleParams =
            LinearLayout.LayoutParams(
                -1,
                -2
            )

        subtitleParams.topMargin =
            25

        root.addView(
            subtitle,
            subtitleParams
        )

        status =
            TextView(this)

        status.textSize =
            22f

        status.setTextColor(
            Color.WHITE
        )

        status.gravity =
            Gravity.CENTER

        val statusParams =
            LinearLayout.LayoutParams(
                -1,
                -2
            )

        statusParams.topMargin =
            35

        root.addView(
            status,
            statusParams
        )

        progress =
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            )

        progress.max = 100

        progress.progress = 0

        val progressParams =
            LinearLayout.LayoutParams(
                -1,
                30
            )

        progressParams.topMargin =
            30

        root.addView(
            progress,
            progressParams
        )

        details =
            TextView(this)

        details.textSize =
            15f

        details.setTextColor(
            Color.LTGRAY
        )

        details.gravity =
            Gravity.CENTER

        val detailsParams =
            LinearLayout.LayoutParams(
                -1,
                -2
            )

        detailsParams.topMargin =
            15

        root.addView(
            details,
            detailsParams
        )

        button =
            Button(this)

        button.text =
            "DOWNLOAD & INSTALL"

        val buttonParams =
            LinearLayout.LayoutParams(
                -1,
                60
            )

        buttonParams.topMargin =
            45

        root.addView(
            button,
            buttonParams
        )

        setContentView(root)
    }

    private fun showDownloadReady() {

        status.text =
            "Gemma 4 E2B"

        details.text =
            "2.58 GB • On-device AI\n" +
                "Private local inference"

        progress.visibility =
            View.VISIBLE

        progress.progress =
            0

        button.isEnabled =
            true

        button.text =
            "DOWNLOAD & INSTALL"

        button.setOnClickListener {
            startDownload()
        }
    }

    private fun startDownload() {

        button.isEnabled =
            false

        status.text =
            "Preparing download..."

        details.text =
            "Please keep JARVIS open."

        lifecycleScope.launch {

            val result =
                downloader.download { data ->

                    runOnUiThread {

                        progress.progress =
                            data.percent

                        status.text =
                            "Downloading Gemma..."

                        val downloaded =
                            formatBytes(
                                data.downloaded
                            )

                        val total =
                            if (data.total > 0L) {
                                formatBytes(
                                    data.total
                                )
                            } else {
                                "Unknown"
                            }

                        val speed =
                            formatBytes(
                                data.speedBytesPerSecond
                            )

                        details.text =
                            "${data.percent}%\n" +
                                "$downloaded / $total\n" +
                                "$speed/s"
                    }
                }

            if (result.isSuccess) {
                showInstalled()
            } else {
                button.isEnabled =
                    true

                button.text =
                    "RETRY DOWNLOAD"

                status.text =
                    "Download failed"

                details.text =
                    result.exceptionOrNull()
                        ?.message
                        ?: "Unknown error"
            }
        }
    }

    private fun showInstalled() {

        status.text =
            "✓ Gemma 4 E2B Installed"

        status.setTextColor(
            Color.rgb(50, 230, 120)
        )

        progress.progress =
            100

        details.text =
            "Local AI is ready.\n" +
                "Model stored securely on this device."

        button.text =
            "LOCAL AI READY"

        button.isEnabled =
            false
    }

    private fun formatBytes(
        bytes: Long
    ): String {

        if (bytes <= 0L) {
            return "0 B"
        }

        val units =
            arrayOf(
                "B",
                "KB",
                "MB",
                "GB"
            )

        var value =
            bytes.toDouble()

        var index =
            0

        while (
            value >= 1024 &&
            index < units.size - 1
        ) {
            value /= 1024.0
            index++
        }

        return String.format(
            Locale.US,
            "%.2f %s",
            value,
            units[index]
        )
    }
}
