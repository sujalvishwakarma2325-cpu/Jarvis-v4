package com.jarvis.v4

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)

        textView.text = """
            JARVIS V4

            System initialized.

            Local AI:
            Gemma 4 E2B

            Status:
            Ready
        """.trimIndent()

        textView.textSize = 20f

        textView.setPadding(
            40,
            80,
            40,
            40
        )

        setContentView(textView)
    }
}
