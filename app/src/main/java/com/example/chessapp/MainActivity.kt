package com.example.chessapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var chessGame: ChessGame
    lateinit var currentLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        currentLanguage = "English"

        val langButton: ImageButton = findViewById(R.id.lang_button)
        langButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        val startResetButton: Button = findViewById(R.id.start_reset_button)
        startResetButton.setOnClickListener {
            chessGame.startOrResetGame()
            updateUI()
        }

        val pauseResumeButton: Button = findViewById(R.id.pause_resume_button)
        pauseResumeButton.setOnClickListener {
            chessGame.pauseOrResumeGame()
            updateUI()
        }
    }

    private fun showLanguageSelectionDialog() {
        val languageOptions = arrayOf("English", "Українська")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if(currentLanguage == "English") "Select Language" else "Оберіть мову")
            .setItems(languageOptions) { _, which ->
                val selectedLanguage = languageOptions[which]
                changeLanguage(selectedLanguage)
            }

        builder.create().show()
    }

    private fun changeLanguage(selectedLanguage: String) {
        currentLanguage = selectedLanguage
        updateUI()
    }

    private fun updateUI() {
        val startResetButton: Button = findViewById(R.id.start_reset_button)
        val pauseResumeButton: Button = findViewById(R.id.pause_resume_button)
        val lastMoveText: TextView = findViewById(R.id.last_move_text)
        val mainTimerText: TextView = findViewById(R.id.main_timer_text)
        val currentMoveText: TextView = findViewById(R.id.current_move_text)
        val currentMoveTV: TextView = findViewById(R.id.current_move_tv)
        val langButton: ImageButton = findViewById(R.id.lang_button)
        val informButton: ImageButton = findViewById(R.id.inf_button)

        if (currentLanguage == "English") {
            startResetButton.text = "Start"
            pauseResumeButton.text = "Pause"
            lastMoveText.text = "Last move:"
            mainTimerText.text = "Game time:"
            currentMoveText.text = "Current move:"
            currentMoveTV.text = if(currentMoveTV.text == "БІЛИЙ\n") "WHITE\n" else "BLACK\n"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = "Starts game or resets it if you started playing"
                pauseResumeButton.tooltipText = "Pauses game or resumes it if you paused it"
                langButton.tooltipText = "Changes app language"
                informButton.tooltipText = "Gives information on how to play the game"
            }
        } else {
            startResetButton.text = "Почати"
            pauseResumeButton.text = "Пауза"
            lastMoveText.text = "Останній хід:"
            mainTimerText.text = "Час гри:"
            currentMoveText.text = "Поточний хід:"
            currentMoveTV.text = if(currentMoveTV.text == "WHITE\n") "БІЛИЙ\n" else "ЧОРНИЙ\n"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = "Запускає гру або скидає її, якщо ви почали грати"
                pauseResumeButton.tooltipText = "Призупиняє гру або відновлює її, якщо ви її призупинили"
                langButton.tooltipText = "Змінює мову програми"
                informButton.tooltipText = "Дає інформацію про те, як грати в гру"
            }
        }
    }
}
