package com.example.chessapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var CURRENT_LANGUAGE = "English"
        var LANG = "eng"
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
    lateinit var soundButton: ImageButton
    lateinit var informButton: ImageButton
    lateinit var exitButton: Button
    lateinit var queenButton: ImageButton
    lateinit var knightButton: ImageButton
    lateinit var rookButton: ImageButton
    lateinit var bishopButton: ImageButton
    lateinit var winText: TextView
    lateinit var board: ChessBoard
    lateinit var timer: GameTimer
    lateinit var musicManager: MusicManager
    private var isGameStarted = false
    private var isGamePaused = true
    private val boardSize = ChessBoard.BOARD_SIZE - 1
    private var isUnMute: Boolean = true
    private var volume: Int = 100
    private var switchText: String = "Music sound (ON)"

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
        soundButton = findViewById(R.id.sound_button)
        langButton = findViewById(R.id.lang_button)
        informButton = findViewById(R.id.inf_button)
        timerTextView = findViewById(R.id.main_timer)
        exitButton = findViewById(R.id.exit_button)
        winText = findViewById(R.id.board_fixer)
        queenButton = findViewById(R.id.queen_button)
        knightButton = findViewById(R.id.knight_button)
        rookButton = findViewById(R.id.rook_button)
        bishopButton = findViewById(R.id.bishop_button)

        musicManager = MusicManager(this)
        musicManager.playMenuMusic()

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

        soundButton.setOnClickListener {
            showSettingsDialog()
        }

        informButton.setOnClickListener {
            showHelpDialog()
        }

        exitButton.setOnClickListener {
            exitGame()
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
        board = ChessBoard(activity, timer, musicManager, winText)
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
        startResetButton.text = resources.getString(resources.getIdentifier("${LANG}_start", "string", packageName))
        pauseGame()
        pauseResumeButton.text = resources.getString(resources.getIdentifier("${LANG}_pause", "string", packageName))
        pauseResumeButton.isEnabled = false
        clearBoard()
        clearGameInfo()
        timer.reset()
        musicManager.playMenuMusic()
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
        startResetButton.text = resources.getString(resources.getIdentifier("${LANG}_reset", "string", packageName))
        pauseResumeButton.isEnabled = true
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = getString(ChessBoard.TEAM_WHITE) + "\n"
        }
        actBoard()
        resumeGame()
        timer.start()
        musicManager.playInGameMusic()
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
        pauseResumeButton.text = resources.getString(resources.getIdentifier("${LANG}_resume", "string", packageName))
        val toastPauseText = resources.getString(resources.getIdentifier("${LANG}_toast_pause", "string", packageName))
        Toast.makeText(activity, toastPauseText, Toast.LENGTH_SHORT).show()
        timer.pause()
        musicManager.pauseMusic()
    }

    private fun resumeGame() {
        isGamePaused = false
        pauseResumeButton.text = resources.getString(resources.getIdentifier("${LANG}_pause", "string", packageName))
        val toastResumeText = resources.getString(resources.getIdentifier("${LANG}_toast_resume", "string", packageName))
        Toast.makeText(activity, toastResumeText, Toast.LENGTH_SHORT).show()
        timer.start()
        musicManager.resumeMusic()
    }

    private fun showLanguageSelectionDialog() {
        val languageOptions = arrayOf("English", "Українська")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(resources.getIdentifier("${LANG}_lang_select", "string", packageName)))
            .setItems(languageOptions) { _, which ->
                val selectedLanguage = languageOptions[which]
                changeLanguage(selectedLanguage)
            }

        builder.create().show()
    }

    private fun changeLanguage(selectedLanguage: String) {
        CURRENT_LANGUAGE = selectedLanguage

        // Update UI elements based on the selected language
        chooseLang()
        updateUI()
    }

    private fun chooseLang() {
        if (CURRENT_LANGUAGE == "English") {
            LANG = "eng"
            return
        }
        if (CURRENT_LANGUAGE == "Українська") {
            LANG = "ukr"
            return
        }
    }

    private fun updateUI() {
        val res = resources // Get the Resources object

        startResetButton.text = if (!isGameStarted) res.getString(res.getIdentifier("${LANG}_start", "string", packageName))
        else res.getString(res.getIdentifier("${LANG}_reset", "string", packageName))
        pauseResumeButton.text = if(!isGamePaused || !isGameStarted) res.getString(res.getIdentifier("${LANG}_pause", "string", packageName))
        else res.getString(res.getIdentifier("${LANG}_resume", "string", packageName))
        exitButton.text = res.getString(res.getIdentifier("${LANG}_exit", "string", packageName))
        lastMoveText.text = res.getString(res.getIdentifier("${LANG}_last_move", "string", packageName))
        mainTimerText.text = res.getString(res.getIdentifier("${LANG}_game_time", "string", packageName))
        currentMoveText.text = res.getString(res.getIdentifier("${LANG}_cur_move", "string", packageName))

        ChessBoard.TEAM_WHITE = res.getIdentifier("${LANG}_white", "string", packageName)
        ChessBoard.TEAM_BLACK = res.getIdentifier("${LANG}_black", "string", packageName)

        currentMoveTV.text = if (currentMoveTV.text == "\n") "\n" else
            if (ChessBoard.CUR_TEAM == "WHITE")
                res.getString(ChessBoard.TEAM_WHITE) + "\n"
            else
                res.getString(ChessBoard.TEAM_BLACK) + "\n"

        winText.text = if (winText.text == "") "" else
            if (ChessBoard.CUR_TEAM == "WHITE")
                res.getString(ChessBoard.TEAM_BLACK) + " " + res.getString(res.getIdentifier("${LANG}_win", "string", packageName))
            else
                res.getString(ChessBoard.TEAM_WHITE) + " " + res.getString(res.getIdentifier("${LANG}_win", "string", packageName))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startResetButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_start", "string", packageName))
            pauseResumeButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_pause", "string", packageName))
            langButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_lang", "string", packageName))
            soundButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_sound", "string", packageName))
            informButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_inf", "string", packageName))
            exitButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_exit", "string", packageName))
            queenButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_queen", "string", packageName))
            knightButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_knight", "string", packageName))
            rookButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_rook", "string", packageName))
            bishopButton.tooltipText = res.getString(res.getIdentifier("${LANG}_tool_bishop", "string", packageName))
        }

        switchText = if (isUnMute) res.getString(res.getIdentifier("${LANG}_sound_not_mute", "string", packageName))
        else res.getString(res.getIdentifier("${LANG}_sound_in_mute", "string", packageName))
    }

    private fun showHelpDialog() {
        val builder = AlertDialog.Builder(this)
        val infTitle = resources.getString(resources.getIdentifier("${LANG}_inf_title", "string", packageName))
        builder.setTitle(infTitle)

        val message = resources.getString(resources.getIdentifier("${LANG}_inf_text", "string", packageName))

        val webView = WebView(this)
        webView.loadDataWithBaseURL(null, message, "text/html", "utf-8", null)

        builder.setView(webView)

        val extTitle = resources.getString(resources.getIdentifier("${LANG}_inf_exit", "string", packageName))

        builder.setPositiveButton(extTitle) { dialog, _ ->
            dialog.dismiss() // Closes the dialog box
        }

        // Prevents closing the dialog box when clicking outside of it, closes only on positive button
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val switchMute = dialogView.findViewById<Switch>(R.id.switchMute)
        val seekBarVolume = dialogView.findViewById<SeekBar>(R.id.seekBarVolume)
        switchMute.text = switchText

        // Set current values
        switchMute.isChecked = isUnMute
        seekBarVolume.progress = volume

        // Save the initial values before displaying the dialog box
        val initialisUnMute = isUnMute
        val initialVolume = volume
        val initialSwitchText = switchText

        val soundTitle = resources.getString(resources.getIdentifier("${LANG}_sound_title", "string", packageName))
        val saveText = resources.getString(resources.getIdentifier("${LANG}_sound_save", "string", packageName))
        val cancelText = resources.getString(resources.getIdentifier("${LANG}_sound_cancel", "string", packageName))

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setTitle(soundTitle)
            .setPositiveButton(saveText) { _, _ ->
                // Save settings only if changes have been made
                if (initialisUnMute != switchMute.isChecked || initialVolume != seekBarVolume.progress) {
                    isUnMute = switchMute.isChecked
                    volume = seekBarVolume.progress

                    musicManager.setMusicVolume(volume)

                    if (isUnMute) {
                        musicManager.setMusicMuted(false)
                        switchText = resources.getString(resources.getIdentifier("${LANG}_sound_not_mute", "string", packageName))
                        soundButton.setBackgroundResource(R.drawable.sound)
                    } else {
                        musicManager.setMusicMuted(true)
                        switchText = resources.getString(resources.getIdentifier("${LANG}_sound_in_mute", "string", packageName))
                        soundButton.setBackgroundResource(R.drawable.no_sound)
                    }
                }
            }
            .setNegativeButton(cancelText) { dialog, _ ->
                // Restore initial values on cancellation
                isUnMute = initialisUnMute
                volume = initialVolume
                switchText = initialSwitchText
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun exitGame() {
        musicManager.stopMusic()
        finish()
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

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    override fun onStop() {
        super.onStop()
        musicManager.pauseMusic()
    }
}
