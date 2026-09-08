package com.jarvis.v4

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.jarvis.v4.ai.ModelDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var downloader:
        ModelDownloader

    private lateinit var status:
        TextView

    private lateinit var details:
        TextView

    private lateinit var progress:
        ProgressBar

    private lateinit var button:
        Button

    private var downloadJob:
        Job? = null

    private val mainScope =
        CoroutineScope(
            Dispatchers.Main +
                SupervisorJob()
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        downloader =
            ModelDownloader(this)

        createScreen()

        if (
            downloader.isInstalled()
        ) {
            showInstalled()
        } else {
            showReady()
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
            70,
            40,
            40
        )

        root.setBackgroundColor(
            Color.rgb(5, 8, 12)
        )

        val title =
            TextView(this)

        title.text =
            "JARVIS V4"

        title.textSize =
            34f

        title.gravity =
            Gravity.CENTER

        title.setTextColor(
            Color.WHITE
        )

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        val subtitle =
            TextView(this)

        subtitle.text =
            "LOCAL AI SETUP"

        subtitle.textSize =
            18f

        subtitle.gravity =
            Gravity.CENTER

        subtitle.setTextColor(
            Color.rgb(
                0,
                210,
                255
            )
        )

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

        status.gravity =
            Gravity.CENTER

        status.setTextColor(
            Color.WHITE
        )

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
                android.R.attr
                    .progressBarStyleHorizontal
            )

        progress.max =
            100

        progress.progress =
            0

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

        details.gravity =
            Gravity.CENTER

        details.setTextColor(
            Color.LTGRAY
        )

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

    private fun showReady() {

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

        downloadJob =
            mainScope.launch {

                val result =
                    downloader.download {

                        data ->

                        runOnUiThread {

                            progress.progress =
                                data.percent

                            status.text =
                                "Downloading Gemma..."

                            details.text =
                                data.percent
                                    .toString() +
                                "%\n" +
                                format(
                                    data.downloaded
                                ) +
                                " / " +
                                format(
                                    data.total
                                ) +
                                "\n" +
                                format(
                                    data.speed
                                ) +
                                "/s"
                        }
                    }

                if (
                    result.isSuccess
                ) {
                    showInstalled()
                } else {

                    button.isEnabled =
                        true

                    button.text =
                        "RETRY DOWNLOAD"

                    status.text =
                        "Download failed"

                    details.text =
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Unknown error"
                }
            }
    }

    private fun showInstalled() {

        status.text =
            "✓ Gemma 4 E2B Installed"

        status.setTextColor(
            Color.rgb(
                50,
                230,
                120
            )
        )

        progress.progress =
            100

        details.text =
            "Local AI is ready.\n" +
            "Model installed on device."

        button.text =
            "LOCAL AI READY"

        button.isEnabled =
            false
    }

    private fun format(
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
            value >= 1024.0 &&
            index <
                units.size - 1
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

    override fun onDestroy() {

        downloadJob?.cancel()

        mainScope.cancel()

        super.onDestroy()
    }
}
