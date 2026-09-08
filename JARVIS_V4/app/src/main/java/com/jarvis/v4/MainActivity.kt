package com.jarvis.v4

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.jarvis.v4.ai.LocalAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private lateinit var localAI:
        LocalAI

    private lateinit var chat:
        TextView

    private lateinit var input:
        EditText

    private lateinit var askButton:
        Button

    private lateinit var status:
        TextView

    private val scope =
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

        localAI =
            LocalAI(this)

        createChatScreen()
    }

    private fun createChatScreen() {

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setBackgroundColor(
            Color.rgb(
                5,
                8,
                12
            )
        )

        root.setPadding(
            25,
            35,
            25,
            20
        )

        val header =
            TextView(this)

        header.text =
            "JARVIS V4"

        header.textSize =
            30f

        header.gravity =
            Gravity.CENTER

        header.setTextColor(
            Color.WHITE
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        status =
            TextView(this)

        status.text =
            "● LOCAL GEMMA • READY TO CHAT"

        status.textSize =
            14f

        status.gravity =
            Gravity.CENTER

        status.setTextColor(
            Color.rgb(
                0,
                210,
                255
            )
        )

        val statusParams =
            LinearLayout.LayoutParams(
                -1,
                -2
            )

        statusParams.topMargin =
            10

        root.addView(
            status,
            statusParams
        )

        val scroll =
            ScrollView(this)

        chat =
            TextView(this)

        chat.text =
            """
            JARVIS:
            Hello. I am JARVIS V4.

            Your Gemma 4 E2B local model
            is installed on this device.

            Ask me something to test
            the local AI brain.
            """.trimIndent()

        chat.textSize =
            17f

        chat.setTextColor(
            Color.WHITE
        )

        chat.setPadding(
            20,
            30,
            20,
            30
        )

        scroll.addView(
            chat
        )

        val scrollParams =
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )

        scrollParams.topMargin =
            20

        root.addView(
            scroll,
            scrollParams
        )

        input =
            EditText(this)

        input.hint =
            "Ask JARVIS..."

        input.textSize =
            16f

        input.setSingleLine(false)

        input.setTextColor(
            Color.WHITE
        )

        input.setHintTextColor(
            Color.GRAY
        )

        input.setBackgroundColor(
            Color.rgb(
                25,
                30,
                36
            )
        )

        input.setPadding(
            20,
            15,
            20,
            15
        )

        root.addView(
            input,
            LinearLayout.LayoutParams(
                -1,
                65
            )
        )

        askButton =
            Button(this)

        askButton.text =
            "ASK JARVIS"

        val buttonParams =
            LinearLayout.LayoutParams(
                -1,
                60
            )

        buttonParams.topMargin =
            12

        root.addView(
            askButton,
            buttonParams
        )

        askButton.setOnClickListener {
            askJarvis()
        }

        setContentView(
            root
        )
    }

    private fun askJarvis() {

        val prompt =
            input.text
                .toString()
                .trim()

        if (prompt.isEmpty()) {

            input.error =
                "Enter a message"

            return
        }

        appendMessage(
            "YOU",
            prompt
        )

        input.text.clear()

        hideKeyboard()

        askButton.isEnabled =
            false

        status.text =
            "● GEMMA IS THINKING..."

        status.setTextColor(
            Color.YELLOW
        )

        scope.launch {

            val result =
                localAI.ask(
                    prompt
                )

            if (result.isSuccess) {

                appendMessage(
                    "JARVIS",
                    result.getOrNull()
                        ?: ""
                )

                status.text =
                    "● LOCAL GEMMA • READY"

                status.setTextColor(
                    Color.rgb(
                        0,
                        210,
                        255
                    )
                )

            } else {

                appendMessage(
                    "ERROR",
                    result
                        .exceptionOrNull()
                        ?.message
                        ?: "Unknown error"
                )

                status.text =
                    "● LOCAL AI ERROR"

                status.setTextColor(
                    Color.RED
                )
            }

            askButton.isEnabled =
                true
        }
    }

    private fun appendMessage(
        speaker: String,
        message: String
    ) {

        val current =
            chat.text
                .toString()

        chat.text =
            current +
                "\n\n" +
                speaker +
                ":\n" +
                message

        chat.post {
            val parent =
                chat.parent

            if (parent is ScrollView) {
                parent.fullScroll(
                    View.FOCUS_DOWN
                )
            }
        }
    }

    private fun hideKeyboard() {

        val manager =
            getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

        manager.hideSoftInputFromWindow(
            input.windowToken,
            0
        )
    }

    override fun onDestroy() {

        localAI.close()

        scope.cancel()

        super.onDestroy()
    }
}
