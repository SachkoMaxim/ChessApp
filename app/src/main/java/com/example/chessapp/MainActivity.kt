package com.example.chessapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var currentLanguage: String
    }

    lateinit var activity: Activity
    lateinit var boardGrid: GridLayout
    lateinit var startResetButton: Button
    lateinit var pauseResumeButton: Button
    lateinit var timerTextView: TextView
    lateinit var lastMoveText: TextView
    lateinit var mainTimerText: TextView
    lateinit var currentMoveText: TextView
    lateinit var currentMoveTV: TextView
    lateinit var langButton: ImageButton
    lateinit var informButton: ImageButton
    lateinit var board: ChessBoard
    lateinit var timer: GameTimer
    private var isGameStarted = false
    private var isGamePaused = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        currentLanguage = "English"

        activity = this
        boardGrid = findViewById(R.id.board_grid)
        startResetButton = findViewById(R.id.start_reset_button)
        pauseResumeButton = findViewById(R.id.pause_resume_button)
        lastMoveText = findViewById(R.id.last_move_text)
        mainTimerText = findViewById(R.id.main_timer_text)
        currentMoveText = findViewById(R.id.current_move_text)
        currentMoveTV = findViewById(R.id.current_move_tv)
        langButton = findViewById(R.id.lang_button)
        informButton = findViewById(R.id.inf_button)
        timerTextView = findViewById(R.id.main_timer)

        timer = GameTimer(timerTextView)

        langButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        startResetButton.setOnClickListener {
            startOrResetGame()
        }

        pauseResumeButton.setOnClickListener {
            pauseOrResumeGame()
        }

        informButton.setOnClickListener {

        }
    }

    private fun showLanguageSelectionDialog() {
        val languageOptions = arrayOf("English", "Українська")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (currentLanguage == "English") "Select Language" else "Оберіть мову")
            .setItems(languageOptions) { _, which ->
                val selectedLanguage = languageOptions[which]
                changeLanguage(selectedLanguage)
            }

        builder.create().show()
    }

    private fun changeLanguage(selectedLanguage: String) {
        currentLanguage = selectedLanguage

        // Update UI elements based on the selected language
        updateUI()
    }

    private fun updateUI() {
        if (currentLanguage == "English") {
            startResetButton.text = if (startResetButton.text == "Почати" ||
                startResetButton.text == "Start") "Start" else "Reset"
            pauseResumeButton.text = if (pauseResumeButton.text == "Пауза" ||
                pauseResumeButton.text == "Pause") "Pause" else "Resume"
            lastMoveText.text = "Last move:"
            mainTimerText.text = "Game time:"
            currentMoveText.text = "Current move:"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = "Starts game or resets it if you started playing"
                pauseResumeButton.tooltipText = "Pauses game or resumes it if you paused it"
                langButton.tooltipText = "Changes app language"
                informButton.tooltipText = "Gives information on how to play the game"
            }
        } else {
            startResetButton.text = if (startResetButton.text == "Start" ||
                startResetButton.text == "Почати") "Почати" else "Скинути"
            pauseResumeButton.text = if (pauseResumeButton.text == "Pause" ||
                pauseResumeButton.text == "Пауза") "Пауза" else "Відновити"
            lastMoveText.text = "Останній хід:"
            mainTimerText.text = "Час гри:"
            currentMoveText.text = "Поточний хід:"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = "Запускає гру або скидає її, якщо ви почали грати"
                pauseResumeButton.tooltipText = "Призупиняє гру або відновлює її, якщо ви її призупинили"
                langButton.tooltipText = "Змінює мову програми"
                informButton.tooltipText = "Дає інформацію про те, як грати в гру"
            }
        }
    }

    private fun actBoard() {
        boardGrid.apply {
            alignmentMode = GridLayout.ALIGN_BOUNDS
            columnCount = 8
            rowCount = 8
        }
        var currentIndex = 0
        for (i in 0..7) {
            for (j in 0..7) {
                val cellButton = createCellButton()
                boardGrid.addView(cellButton, currentIndex++)

                val param = createCellLayoutParams(j, i)
                cellButton.layoutParams = param
            }
        }
        board = ChessBoard(activity)
        board.init(boardGrid, this)
        board.show()
    }

    private fun createCellButton(): Button {
        val cellButton = Button(activity)
        cellButton.textSize = 5f
        cellButton.setOnClickListener {
            // Handle button click
        }
        return cellButton
    }

    private fun createCellLayoutParams(j: Int, i: Int): GridLayout.LayoutParams {
        return GridLayout.LayoutParams().apply {
            height = boardGrid.width / 8
            width = boardGrid.width / 8
            bottomMargin = 1
            leftMargin = 0
            topMargin = 0
            rightMargin = 1
            columnSpec = GridLayout.spec(j)
            rowSpec = GridLayout.spec(7 - i)
        }
    }

    private fun clearBoard() {
        val boardGrid: GridLayout = activity.findViewById(R.id.board_grid)
        boardGrid.removeAllViews()
    }

    private fun startOrResetGame() {
        if (!isGameStarted) {
            startGame()
        } else {
            resetGame()
        }
    }

    private fun resetGame() {
        isGameStarted = false
        val startResText = if (currentLanguage == "English") "Start" else "Почати"
        startResetButton.text = startResText
        pauseGame()
        val pauseResText = if (currentLanguage == "English") "Pause" else "Пауза"
        pauseResumeButton.text = pauseResText
        pauseResumeButton.isEnabled = false
        clearBoard()
        clearGameInfo()
        timer.reset()
    }

    private fun clearGameInfo() {
        activity.findViewById<TextView>(R.id.last_move_tv).apply {
            text = "\n"
        }
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = "\n"
        }
    }

    private fun startGame() {
        isGameStarted = true
        val startResText = if (currentLanguage == "English") "Reset" else "Скинути"
        startResetButton.text = startResText
        pauseResumeButton.isEnabled = true
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = "WHITE\n"
        }
        actBoard()
        resumeGame()
        timer.start()
    }

    private fun pauseOrResumeGame() {
        if (board.getIsCheckmate()) {
            return
        }
        if (isGamePaused) {
            resumeGame()
        } else {
            pauseGame()
        }
        board.switchButtonsBlocking(isGamePaused)
    }

    private fun pauseGame() {
        isGamePaused = true
        val pauseResText = if (currentLanguage == "English") "Resume" else "Відновити"
        pauseResumeButton.text = pauseResText
        val toastPauseText = if (currentLanguage == "English") "Game is paused" else "Гру зупинено"
        Toast.makeText(activity, toastPauseText, Toast.LENGTH_SHORT).show()
        timer.pause()
    }

    private fun resumeGame() {
        isGamePaused = false
        val pauseResText = if (currentLanguage == "English") "Pause" else "Пауза"
        pauseResumeButton.text = pauseResText
        val toastResumeText = if (currentLanguage == "English") "Game is resumed" else "Гру відновлено"
        Toast.makeText(activity, toastResumeText, Toast.LENGTH_SHORT).show()
        timer.start()
    }
}
