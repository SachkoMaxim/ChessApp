package com.example.chessapp

import android.app.Activity
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Collections

class ChessBoard(activity: Activity, timer: GameTimer, winText: TextView) {

    companion object {
        const val BOARD_SIZE = 8
        const val WHITE = "WHITE"
        const val BLACK = "BLACK"
        var TEAM_WHITE = if(MainActivity.CURRENT_LANGUAGE == "English") R.string.eng_white else R.string.ukr_white
        var TEAM_BLACK = if(MainActivity.CURRENT_LANGUAGE == "English") R.string.eng_black else R.string.ukr_black
    }

    var canLongCastlingWhite = false
    var canLongCastlingBlack = false
    var canShortCastlingWhite = false
    var canShortCastlingBlack = false

    private val activity = activity
    private val timer = timer
    private var winText = winText
    private val boardSize = BOARD_SIZE - 1
    private val cells: Array<Array<Cell?>> = Array(BOARD_SIZE) { Array(BOARD_SIZE) { null } }
    private lateinit var whiteCells: MutableList<Cell>
    private lateinit var blackCells: MutableList<Cell>
    private lateinit var gridLayout: GridLayout
    private lateinit var mainActivity: MainActivity

    private val startMap = arrayOf(
        arrayOf<Piece?>(
            Rook(WHITE), Knight(WHITE), Bishop(WHITE), Queen(WHITE), King(WHITE), Bishop(WHITE), Knight(WHITE), Rook(WHITE)
        ),
        arrayOf<Piece?>(
            Pawn(WHITE), Pawn(WHITE), Pawn(WHITE), Pawn(WHITE), Pawn(WHITE), Pawn(WHITE), Pawn(WHITE), Pawn(WHITE)
        ),
        arrayOf<Piece?>(null, null, null, null, null, null, null, null),
        arrayOf<Piece?>(null, null, null, null, null, null, null, null),
        arrayOf<Piece?>(null, null, null, null, null, null, null, null),
        arrayOf<Piece?>(null, null, null, null, null, null, null, null),
        arrayOf<Piece?>(
            Pawn(BLACK), Pawn(BLACK), Pawn(BLACK), Pawn(BLACK), Pawn(BLACK), Pawn(BLACK), Pawn(BLACK), Pawn(BLACK)
        ),
        arrayOf<Piece?>(
            Rook(BLACK), Knight(BLACK), Bishop(BLACK), Queen(BLACK), King(BLACK), Bishop(BLACK), Knight(BLACK), Rook(BLACK)
        ),
    )

    private var selectedCell: Cell? = null
    private var currentTeam = WHITE
    private var possibleMoves = mutableListOf<Pair<Int, Int>>()
    private var isMoveStarted = false
    private var isCheck = false
    private var isCheckmate = false

    fun init(gridLayout: GridLayout, mainActivity: MainActivity) {
        this.gridLayout = gridLayout
        this.mainActivity = mainActivity
        isCheck = false
        isCheckmate = false
        val cellsMutableList = mutableListOf<MutableList<Cell>>()
        var childCounter = 0
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                val button = gridLayout.getChildAt(childCounter++)!! as Button
                val newCell = Cell(button, startMap[i][j], this)
                newCell.setCoords(i, j)
                cellsMutableList.add(mutableListOf())
                cellsMutableList[i].add(newCell)
            }
            cells[i] = cellsMutableList[i].toTypedArray()
        }
        whiteCells = setCellsList(WHITE)
        blackCells = setCellsList(BLACK)
        setCellButtonsOnClickListeners()
    }

    private fun setCellsList(color: String): MutableList<Cell> {
        val colorCells = mutableListOf<Cell>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (cells[i][j]!!.piece?.color == color) {
                    colorCells.add(cells[i][j]!!)
                }
            }
        }
        return colorCells
    }

    private fun setCellButtonsOnClickListeners() {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                val cell = cells[i][j]!!
                cell.button.setOnClickListener {
                    Log.d("Cell click", "You clicked on cell ${getChessCoords(cell.getX()!!, cell.getY()!!)}!")
                    if (cell.piece != null) {
                        Log.d("Cell click", "The piece is ${cell.piece!!::class.java.simpleName}")
                    } else {
                        Log.d("Cell click", "There is no piece on this cell")
                    }
                    doMove(cell)
                    show()
                }
            }
        }
    }

    fun switchButtonsBlocking(isGamePaused: Boolean) {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                cells[i][j]!!.button.isClickable = !isGamePaused
            }
        }
    }

    fun show() {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (cells[i][j]!!.piece != null) {
                    cells[i][j]!!.button.setBackgroundResource(cells[i][j]!!.piece!!.getDrawableID())
                } else {
                    cells[i][j]!!.button.setBackgroundResource(R.drawable.transparent)
                }
            }
        }
    }

    private fun getChessCoords(x: Int, y: Int): String {
        return "${x + 1}${(y.toChar() + 97)}"
    }

    fun getCells() = cells

    private fun doMove(cell: Cell) {
        if (!isMoveStarted && cell.piece != null && cell.piece?.color == currentTeam) {
            startMove(cell)
        } else if (isMoveStarted && cell.piece != null && cell.piece?.color == currentTeam) {
            startMove(cell)
        } else if (isMoveStarted && cell.piece?.color != currentTeam) {
            finishMove(cell)
        }
    }

    private fun startMove(cell: Cell) {
        selectedCell = cell
        possibleMoves = selectedCell!!.getPossibleMoves()
        if (selectedCell!!.piece is King) {
            checkForLongCastling(selectedCell!!)
            checkForShortCastling(selectedCell!!)
        }
        isMoveStarted = true
    }

    private fun finishMove(cell: Cell) {
        if (isMoveStarted) {
            if (possibleMoves.any { pair -> pair.first == cell.getX() && pair.second == cell.getY() }) {

                val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells
                if (selectedCell!!.piece is King && cell.isUnderAttack(enemyCells)) {
                    // Destination cell is under attack, prevent the move
                    return
                }

                activity.findViewById<TextView>(R.id.last_move_tv).apply {
                    text = "${getChessCoords(selectedCell!!.getX()!!, selectedCell!!.getY()!!)} " +
                            "${getChessCoords(cell.getX()!!, cell.getY()!!)} \n"
                }
                movePiece(selectedCell!!, cell)
                selectedCell = null
                isMoveStarted = false
                if (!(cell.piece is Pawn && (cell.getX() == 0 || cell.getX() == boardSize))) {
                    switchCurrentTeam()
                }

                checkForCheckmate()
            }
        }
    }

    private fun movePiece(selectedCell: Cell, cell: Cell) {
        // Check if the moving piece is a king and is making a castling move
        if (selectedCell.piece is King && (cell.getX()!! - selectedCell.getX()!!) == 0 &&
            Math.abs(cell.getY()!! - selectedCell.getY()!!) == 2
        ) {
            // King is making a castling move
            val direction = if (cell.getY()!! - selectedCell.getY()!! > 0) 1 else -1
            val rookStartCol = if (direction > 0) boardSize else 0

            val rook = cells[selectedCell.getX()!!][rookStartCol]!!
            val rookDestination = cells[selectedCell.getX()!!][selectedCell.getY()!! + direction]!!

            // Move the rook to the destination cell
            moveNormalPiece(rook, rookDestination)
        }

        // Move the selected piece to the destination cell
        moveNormalPiece(selectedCell, cell)
    }

    private fun moveNormalPiece(selectedCell: Cell, cell: Cell) {
        if (cell.piece == null) {
            cell.piece = selectedCell.piece
            selectedCell.piece = null
        } else {
            cell.piece = selectedCell.piece
            selectedCell.piece = null
            val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells
            enemyCells.remove(cell)
        }

        if (cell.piece is Pawn) {
            checkForPromotion(cell)
        }

        cell.piece!!.setIsMoved()

        val currentTeamCells = if (currentTeam == WHITE) whiteCells else blackCells
        Collections.replaceAll(currentTeamCells, selectedCell, cell)

        show()
    }

    private fun checkForPromotion(cell: Cell) {
        if (cell.piece!!.color == WHITE && cell.getX() == boardSize ||
            cell.piece!!.color == BLACK && cell.getX() == 0
        ) {
            mainActivity.promoteButtons(true)
            switchButtonsBlocking(true)
            val promotionText = if(MainActivity.CURRENT_LANGUAGE == "English") activity.getString(R.string.eng_toast_promote)
                else activity.getString(R.string.ukr_toast_promote)
            Toast.makeText(activity, "${promotionText}", Toast.LENGTH_SHORT).show()
            promotePawn(cell)
        }
    }

    fun promotePawn(cell: Cell) {
        // Handler for clicking on the "Queen" button
        mainActivity.queenButton.setOnClickListener {
            cell.promotePawnTo(Queen(currentTeam))
            changeTeamAfterProm()
        }

        // Handler for clicking on the "Knight" button
        mainActivity.knightButton.setOnClickListener {
            cell.promotePawnTo(Knight(currentTeam))
            changeTeamAfterProm()
        }

        // Handler for clicking on the "Bishop" button
        mainActivity.bishopButton.setOnClickListener {
            cell.promotePawnTo(Bishop(currentTeam))
            changeTeamAfterProm()
        }

        // Handler for clicking on the "Rook" button
        mainActivity.rookButton.setOnClickListener {
            cell.promotePawnTo(Rook(currentTeam))
            changeTeamAfterProm()
        }
    }

    fun changeTeamAfterProm() {
        mainActivity.promoteButtons(false)
        switchButtonsBlocking(false)
        switchCurrentTeam()
        show()
    }

    private fun switchCurrentTeam() {
        val currentTeamText = if (currentTeam == WHITE) {
            activity.getString(TEAM_BLACK)
        } else {
            activity.getString(TEAM_WHITE)
        }
        currentTeam = if (currentTeam == WHITE) {
            BLACK
        } else {
            WHITE
        }
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = currentTeamText + "\n"
        }
    }

    fun checkForCastling(king: Cell, isShortCastling: Boolean) {
        if (!king.piece!!.getIsMoved()) {
            // Determine the direction for castling
            val direction = if (isShortCastling) 1 else -1

            // Checking if rook hasn't moved
            val rookCol = if (isShortCastling) boardSize else 0
            val rook = cells[king.getX()!!][rookCol]!!

            if (rook.piece is Rook && !rook.isRookMoved()) {
                // Checking whether the cells between king and rook are free
                val startCol = if (isShortCastling) king.getY()!! + 1 else 1
                val endCol = if (isShortCastling) boardSize else king.getY()!!

                for (col in startCol until endCol) {
                    if (cells[king.getX()!!][col]!!.piece != null) {
                        // There is a figure on the path of castling
                        setCastlingDef(isShortCastling, king.piece!!.color, false)
                        return
                    }
                }

                // Checking that king is not in check and that his newly marked cell is not attacked
                val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells
                val destinationCol = king.getY()!! + 2 * direction
                val destinationCell = cells[king.getX()!!][destinationCol]!!

                if (!king.isUnderAttack(enemyCells) && !destinationCell.isUnderAttack(enemyCells)) {
                    setCastlingDef(isShortCastling, king.piece!!.color, true)
                } else {
                    setCastlingDef(isShortCastling, king.piece!!.color, false)
                }
            } else {
                setCastlingDef(isShortCastling, king.piece!!.color, false)
            }
        } else {
            setCastlingDef(isShortCastling, king.piece!!.color, false)
        }
    }

    private fun setCastlingDef(isShortCastling: Boolean, color: String, definition: Boolean) {
        if (isShortCastling == true) {
            if (color == WHITE) {
                canShortCastlingWhite = definition
            } else {
                canShortCastlingBlack = definition
            }
        } else {
            if (color == WHITE) {
                canLongCastlingWhite = definition
            } else {
                canLongCastlingBlack = definition
            }
        }
    }

    private fun checkForLongCastling(king: Cell) {
        checkForCastling(king, false)
    }

    private fun checkForShortCastling(king: Cell) {
        checkForCastling(king, true)
    }

    fun checkForCheckmate() {
        val king = if (currentTeam == WHITE) whiteCells.find { it.piece is King } else blackCells.find { it.piece is King }
        val enemyTeamText = if (currentTeam == WHITE) activity.getString(TEAM_BLACK) else activity.getString(TEAM_WHITE)
        val curTeamText = if (currentTeam == WHITE) activity.getString(TEAM_WHITE) else activity.getString(TEAM_BLACK)

        if (king != null) {
            val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells
            val attackingCell = getAttackingCell(king, enemyCells)

            if (isKingUnderAttack(king, enemyCells)) {
                if (hasLegalMoves(king) || isAttackingCellUnderAttack(attackingCell!!)) {
                    // The king is in check, but there are legal moves or pieces that can attack attacking piece
                    isCheckmate = false
                    Log.d("Check", "${currentTeam} is in check.")
                    val toastCheck = if(MainActivity.CURRENT_LANGUAGE == "English") activity.getString(R.string.eng_toast_check)
                        else activity.getString(R.string.ukr_toast_check)
                    Toast.makeText(activity, "${curTeamText} ${toastCheck}", Toast.LENGTH_SHORT).show()
                } else {
                    // The king is in checkmate
                    isCheckmate = true
                    Log.d("Checkmate", "Game over! ${currentTeam} is in checkmate.")
                    onGameOver(enemyTeamText)
                }
            }
        } else {
            // One team's king is missing, game over
            isCheckmate = true
            Log.d("Checkmate", "Game over! ${currentTeam} has no king.")
            onGameOver(enemyTeamText)
        }
    }

    private fun getAttackingCell(king: Cell, enemyCells: List<Cell>): Cell? {
        // Getting pieces that attack the king
        val attackingPieces = enemyCells.filter { cell ->
            cell.getPossibleMoves().any { move ->
                move.first == king.getX() && move.second == king.getY()
            }
        }
        Log.d("Attacking pieces", "Attacking pieces are: $attackingPieces.")

        // If there are attacking pieces, return the cell that is attacking
        return if (attackingPieces.isNotEmpty()) attackingPieces[0] else null
    }

    private fun isAttackingCellUnderAttack(attackingCell: Cell): Boolean {
        val enemyCells = if (currentTeam == WHITE) whiteCells else blackCells

        return isCellUnderAttack(attackingCell.getX()!!, attackingCell.getY()!!, enemyCells)
    }

    private fun isKingUnderAttack(king: Cell, enemyCells: List<Cell>): Boolean {
        return enemyCells.any { cell ->
            cell.getPossibleMoves()
                .any { move -> move.first == king.getX()!! && move.second == king.getY()!! }
        }
    }

    private fun isCellUnderAttack(x: Int, y: Int, enemyCells: List<Cell>): Boolean {
        // Checking whether any enemy piece can attack the given cell
        return enemyCells.any { cell ->
            cell.getPossibleMoves().any { move -> move.first == x && move.second == y }
        }
    }

    private fun hasLegalMoves(king: Cell): Boolean {
        // Get all the possible moves for the king
        val possibleMoves = king.getPossibleMoves()

        // Get enemy pieces
        val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells

        // Check each possible move of the king
        for (move in possibleMoves) {
            // Check if the cell to which the king can move is not under attack by the enemy
            if (!isCellUnderAttack(move.first, move.second, enemyCells)) {
                return true
            }
        }

        // If none of the cells are safe, return false
        return false
    }

    private fun onGameOver(team: String) {
        val toastMate = if(MainActivity.CURRENT_LANGUAGE == "English") activity.getString(R.string.eng_toast_mate)
            else activity.getString(R.string.ukr_toast_mate)
        val winEnd = if(MainActivity.CURRENT_LANGUAGE == "English") "WON!"
            else "ВИГРАВ!"
        winText.text = "${team} ${winEnd}"
        Toast.makeText(activity, "${team} ${toastMate}", Toast.LENGTH_SHORT).show()
        switchButtonsBlocking(true)
        timer.pause()
    }

    fun getIsCheckmate(): Boolean = isCheckmate
}
