package com.example.chessapp

import android.app.Activity
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Collections

class ChessBoard(activity: Activity) {

    companion object {
        const val BOARD_SIZE = 8
        var WHITE = "WHITE"
        var BLACK = "BLACK"
    }

    var canLongCastlingWhite = false
    var canLongCastlingBlack = false
    var canShortCastlingWhite = false
    var canShortCastlingBlack = false

    private val activity = activity
    private val cells: Array<Array<Cell?>> = Array(BOARD_SIZE) { Array(BOARD_SIZE) { null } }
    private lateinit var whiteCells: MutableList<Cell>
    private lateinit var blackCells: MutableList<Cell>
    private var threateningCells = mutableListOf<Cell>()
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
    private var isMate = false

    fun init(gridLayout: GridLayout, mainActivity: MainActivity) {
        this.gridLayout = gridLayout
        this.mainActivity = mainActivity
        isCheck = false
        isMate = false
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
                if (selectedCell!!.piece is King && cell.isUnderAttack(enemyCells, true)) {
                    // Destination cell is under attack, prevent the move
                    return
                }

                activity.findViewById<TextView>(R.id.last_move_tv).apply {
                    text = "${getChessCoords(selectedCell!!.getX()!!, selectedCell!!.getY()!!)} ${getChessCoords(cell.getX()!!, cell.getY()!!)} \n"
                }
                movePiece(selectedCell!!, cell)
                selectedCell = null
                isMoveStarted = false
                switchCurrentTeam()

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
            val rookStartCol = if (direction > 0) BOARD_SIZE - 1 else 0

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
        if (cell.piece!!.color == WHITE && cell.getX() == BOARD_SIZE - 1 ||
            cell.piece!!.color == BLACK && cell.getX() == 0
        ) {
            cell.promotePawn()
        }
    }

    private fun switchCurrentTeam() {
        currentTeam = if (currentTeam == WHITE) {
            BLACK
        } else {
            WHITE
        }
        activity.findViewById<TextView>(R.id.current_move_tv).apply {
            text = currentTeam + "\n"
        }
    }

    fun checkForCastling(king: Cell, isShortCastling: Boolean) {
        if (!king.piece!!.getIsMoved()) {
            // Determine the direction for castling
            val direction = if (isShortCastling) 1 else -1

            // Checking if rook hasn't moved
            val rookCol = if (isShortCastling) BOARD_SIZE - 1 else 0
            val rook = cells[king.getX()!!][rookCol]!!

            if (rook.piece is Rook && !rook.isRookMoved()) {
                // Checking whether the cells between king and rook are free
                val startCol = if (isShortCastling) king.getY()!! + 1 else 1
                val endCol = if (isShortCastling) BOARD_SIZE - 1 else king.getY()!!

                for (col in startCol until endCol) {
                    if (cells[king.getX()!!][col]!!.piece != null) {
                        // There is a figure on the path of castling
                        setCannotCastling(isShortCastling, king.piece!!.color)
                        return
                    }
                }

                // Checking that king is not in check and that his newly marked cell is not attacked
                val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells
                val destinationCol = king.getY()!! + 2 * direction
                val destinationCell = cells[king.getX()!!][destinationCol]!!

                if (!king.isUnderAttack(enemyCells, true) && !destinationCell.isUnderAttack(enemyCells, true)) {
                    setCanCastling(isShortCastling, king.piece!!.color)
                } else {
                    setCannotCastling(isShortCastling, king.piece!!.color)
                }
            } else {
                setCannotCastling(isShortCastling, king.piece!!.color)
            }
        } else {
            setCannotCastling(isShortCastling, king.piece!!.color)
        }
    }

    private fun setCanCastling(isShortCastling: Boolean, color: String) {
        if(isShortCastling == true) {
            if(color == WHITE) {
                canShortCastlingWhite = true
            } else {
                canShortCastlingBlack = true
            }
        } else {
            if(color == WHITE) {
                canLongCastlingWhite = true
            } else {
                canLongCastlingBlack = true
            }
        }
    }

    private fun setCannotCastling(isShortCastling: Boolean, color: String) {
        if(isShortCastling == true) {
            if(color == WHITE) {
                canShortCastlingWhite = false
            } else {
                canShortCastlingBlack = false
            }
        } else {
            if(color == WHITE) {
                canLongCastlingWhite = false
            } else {
                canLongCastlingBlack = false
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
        val enemyTeam = if (currentTeam == WHITE) BLACK else WHITE

        if (king != null) {
            val enemyCells = if (currentTeam == WHITE) blackCells else whiteCells

            // Check if the king is under attack
            if (king.isUnderAttack(enemyCells)) {
                // Check if the king has any legal moves
                val legalMoves = king.getPossibleMoves(true)
                if (legalMoves.isEmpty()) {
                    // The king is in checkmate
                    isMate = true
                    Log.d("Checkmate", "Game over! ${currentTeam} is in checkmate.")
                    onGameOver(enemyTeam)
                } else {
                    // Check if there is any piece that can block or capture the attacking piece
                    val attackingPiece = enemyCells.find { cell ->
                        cell.getPossibleMoves(true).any { move ->
                            move.first == king.getX()!! && move.second == king.getY()!!
                        }
                    }

                    if (attackingPiece != null) {
                        // The king is in check, but there are legal moves or pieces that can intervene
                        isMate = false
                        Log.d("Check", "${currentTeam} is in check.")
                        Toast.makeText(activity, "${currentTeam} is in check.", Toast.LENGTH_SHORT).show()
                    } else {
                        // No legal moves or pieces to intervene, it's checkmate
                        isMate = true
                        Log.d("Checkmate", "Game over! ${currentTeam} is in checkmate.")
                        onGameOver(enemyTeam)
                    }
                }
            }
        } else {
            // One team's king is missing, game over
            isMate = true
            Log.d("Checkmate", "Game over! ${currentTeam} has no king.")
            onGameOver(enemyTeam)
        }
    }

    private fun onGameOver(team: String) {
        Toast.makeText(activity, "${team} has made a mate! Game over!", Toast.LENGTH_SHORT).show()
        switchButtonsBlocking(true)
    }
}
