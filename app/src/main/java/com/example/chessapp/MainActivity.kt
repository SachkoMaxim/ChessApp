package com.example.chessapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var CURRENT_LANGUAGE = "English"
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
    lateinit var exitButton: Button
    lateinit var queenButton: ImageButton
    lateinit var knightButton: ImageButton
    lateinit var rookButton: ImageButton
    lateinit var bishopButton: ImageButton
    lateinit var winText: TextView
    lateinit var board: ChessBoard
    lateinit var timer: GameTimer
    private var isGameStarted = false
    private var isGamePaused = true
    private val boardSize = ChessBoard.BOARD_SIZE - 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

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
        exitButton = findViewById(R.id.exit_button)
        winText = findViewById(R.id.board_fixer)
        queenButton = findViewById(R.id.queen_button)
        knightButton = findViewById(R.id.knight_button)
        rookButton = findViewById(R.id.rook_button)
        bishopButton = findViewById(R.id.bishop_button)

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
            showHelpDialog()
        }

        exitButton.setOnClickListener {
            exitGame()
        }
    }

    private fun showLanguageSelectionDialog() {
        val languageOptions = arrayOf("English", "Українська")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_lang_select) else getString(R.string.ukr_lang_select))
            .setItems(languageOptions) { _, which ->
                val selectedLanguage = languageOptions[which]
                changeLanguage(selectedLanguage)
            }

        builder.create().show()
    }

    private fun changeLanguage(selectedLanguage: String) {
        CURRENT_LANGUAGE = selectedLanguage

        // Update UI elements based on the selected language
        updateUI()
    }

    private fun updateUI() {
        if (CURRENT_LANGUAGE == "English") {
            startResetButton.text = if (startResetButton.text == getString(R.string.ukr_start) ||
                startResetButton.text == getString(R.string.eng_start))
                    getString(R.string.eng_start) else getString(R.string.eng_reset)
            pauseResumeButton.text = if (pauseResumeButton.text == getString(R.string.ukr_pause) ||
                pauseResumeButton.text == getString(R.string.eng_pause))
                    getString(R.string.eng_pause) else getString(R.string.eng_resume)
            exitButton.text = getString(R.string.eng_exit)
            lastMoveText.text = getString(R.string.eng_last_move)
            mainTimerText.text = getString(R.string.eng_game_time)
            currentMoveText.text = getString(R.string.eng_cur_move)
            ChessBoard.TEAM_WHITE = R.string.eng_white
            ChessBoard.TEAM_BLACK = R.string.eng_black
            currentMoveTV.text = if (currentMoveTV.text == "\n") "\n" else
                if (currentMoveTV.text == getString(R.string.eng_white) + "\n" ||
                    currentMoveTV.text == getString(R.string.ukr_white) + "\n")
                        getString(ChessBoard.TEAM_WHITE) + "\n"
                            else getString(ChessBoard.TEAM_BLACK) + "\n"
            winText.text = if (winText.text == "") "" else
                if (winText.text == getString(R.string.eng_white) + " WON!" ||
                    winText.text == getString(R.string.ukr_white) + " ВИГРАВ!")
                        getString(ChessBoard.TEAM_WHITE) + " WON!"
                            else getString(ChessBoard.TEAM_BLACK) + " WON!"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = getString(R.string.eng_tool_start)
                pauseResumeButton.tooltipText = getString(R.string.eng_tool_pause)
                langButton.tooltipText = getString(R.string.eng_tool_lang)
                informButton.tooltipText = getString(R.string.eng_tool_inf)
                exitButton.tooltipText = getString(R.string.eng_tool_exit)
                queenButton.tooltipText = getString(R.string.eng_tool_queen)
                knightButton.tooltipText = getString(R.string.eng_tool_knight)
                rookButton.tooltipText = getString(R.string.eng_tool_rook)
                bishopButton.tooltipText = getString(R.string.eng_tool_bishop)
            }
        } else {
            startResetButton.text = if (startResetButton.text == getString(R.string.eng_start) ||
                startResetButton.text == getString(R.string.ukr_start))
                    getString(R.string.ukr_start) else getString(R.string.ukr_reset)
            pauseResumeButton.text = if (pauseResumeButton.text == getString(R.string.eng_pause) ||
                pauseResumeButton.text == getString(R.string.ukr_pause))
                    getString(R.string.ukr_pause) else getString(R.string.ukr_resume)
            exitButton.text = getString(R.string.ukr_exit)
            lastMoveText.text = getString(R.string.ukr_last_move)
            mainTimerText.text = getString(R.string.ukr_game_time)
            currentMoveText.text = getString(R.string.ukr_cur_move)
            ChessBoard.TEAM_WHITE = R.string.ukr_white
            ChessBoard.TEAM_BLACK = R.string.ukr_black
            currentMoveTV.text = if (currentMoveTV.text == "\n") "\n" else
                if (currentMoveTV.text == getString(R.string.ukr_white) + "\n" ||
                    currentMoveTV.text == getString(R.string.eng_white) + "\n")
                        getString(ChessBoard.TEAM_WHITE) + "\n"
                            else getString(ChessBoard.TEAM_BLACK) + "\n"
            winText.text = if (winText.text == "") "" else
                if (winText.text == getString(R.string.eng_white) + " WON!" ||
                    winText.text == getString(R.string.ukr_white) + " ВИГРАВ!")
                        getString(ChessBoard.TEAM_WHITE) + " ВИГРАВ!"
                            else getString(ChessBoard.TEAM_BLACK) + " ВИГРАВ!"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startResetButton.tooltipText = getString(R.string.ukr_tool_start)
                pauseResumeButton.tooltipText = getString(R.string.ukr_tool_pause)
                langButton.tooltipText = getString(R.string.ukr_tool_lang)
                informButton.tooltipText = getString(R.string.ukr_tool_inf)
                exitButton.tooltipText = getString(R.string.ukr_tool_exit)
                queenButton.tooltipText = getString(R.string.ukr_tool_queen)
                knightButton.tooltipText = getString(R.string.ukr_tool_knight)
                rookButton.tooltipText = getString(R.string.ukr_tool_rook)
                bishopButton.tooltipText = getString(R.string.ukr_tool_bishop)
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
        for (i in 0 until ChessBoard.BOARD_SIZE) {
            for (j in 0 until ChessBoard.BOARD_SIZE) {
                val cellButton = createCellButton()
                boardGrid.addView(cellButton, currentIndex++)

                val param = createCellLayoutParams(j, i)
                cellButton.layoutParams = param
            }
        }
        board = ChessBoard(activity, timer, winText)
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
            rowSpec = GridLayout.spec(boardSize - i)
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
        val startResText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_start) else getString(R.string.ukr_start)
        startResetButton.text = startResText
        pauseGame()
        val pauseResText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_pause) else getString(R.string.ukr_pause)
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
        activity.findViewById<TextView>(R.id.board_fixer).apply {
            text = ""
        }
    }

    private fun startGame() {
        isGameStarted = true
        val startResText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_reset) else getString(R.string.ukr_reset)
        startResetButton.text = startResText
        pauseResumeButton.isEnabled = true
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = getString(ChessBoard.TEAM_WHITE) + "\n"
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
        val pauseResText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_resume) else getString(R.string.ukr_resume)
        pauseResumeButton.text = pauseResText
        val toastPauseText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_toast_pause) else getString(R.string.ukr_toast_pause)
        Toast.makeText(activity, toastPauseText, Toast.LENGTH_SHORT).show()
        timer.pause()
    }

    private fun resumeGame() {
        isGamePaused = false
        val pauseResText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_pause) else getString(R.string.ukr_pause)
        pauseResumeButton.text = pauseResText
        val toastResumeText = if (CURRENT_LANGUAGE == "English")
            getString(R.string.eng_toast_resume) else getString(R.string.ukr_toast_resume)
        Toast.makeText(activity, toastResumeText, Toast.LENGTH_SHORT).show()
        timer.start()
    }

    fun promoteButtons(show: Boolean) {
        if (show == true) {
            queenButton.visibility = View.VISIBLE
            knightButton.visibility = View.VISIBLE
            bishopButton.visibility = View.VISIBLE
            rookButton.visibility = View.VISIBLE
        } else {
            queenButton.visibility = View.INVISIBLE
            knightButton.visibility = View.INVISIBLE
            bishopButton.visibility = View.INVISIBLE
            rookButton.visibility = View.INVISIBLE
        }
        queenButton.isEnabled = show
        knightButton.isEnabled = show
        bishopButton.isEnabled = show
        rookButton.isEnabled = show

    }

    fun showHelpDialog() {
        val builder = AlertDialog.Builder(this)
        val infTitle = if (CURRENT_LANGUAGE == "English") getString(R.string.eng_inf_title)
            else getString(R.string.ukr_inf_title)
        builder.setTitle(infTitle)

        val message = if (CURRENT_LANGUAGE == "English") getString(R.string.eng_inf_text)
            else getString(R.string.ukr_inf_text)

        val webView = WebView(this)
        webView.loadDataWithBaseURL(null, message, "text/html", "utf-8", null)

        builder.setView(webView)

        val extTitle = if (CURRENT_LANGUAGE == "English") getString(R.string.eng_inf_exit)
            else getString(R.string.ukr_inf_exit)

        builder.setPositiveButton(extTitle) { dialog, _ ->
            dialog.dismiss() // Closes the dialog box
        }

        // Prevents closing the dialog box when clicking outside of it, closes only on positive button
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun exitGame() {
        activity.finishAffinity()
    }
}
