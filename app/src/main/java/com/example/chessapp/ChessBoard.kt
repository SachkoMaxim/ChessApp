package com.example.chessapp

import android.app.Activity
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import java.util.Collections

class ChessBoard(activity: Activity) {

    companion object {
        const val BOARD_SIZE = 8
        var WHITE = "WHITE"
        var BLACK = "BLACK"
    }

    private val activity = activity
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
            selectedCell = cell
            possibleMoves = selectedCell!!.getPossibleMoves()
            isMoveStarted = true
        } else if (isMoveStarted && cell.piece != null && cell.piece?.color == currentTeam) {
            selectedCell = cell
            possibleMoves = selectedCell!!.getPossibleMoves()
        } else if (isMoveStarted && cell.piece?.color != currentTeam) {
            if (possibleMoves.any { pair -> pair.first == cell.getX() && pair.second == cell.getY() }) {
                activity.findViewById<TextView>(R.id.last_move_tv).apply {
                    text = "${getChessCoords(selectedCell!!.getX()!!, selectedCell!!.getY()!!)} ${getChessCoords(cell.getX()!!, cell.getY()!!)} \n"
                }
                movePiece(selectedCell!!, cell)
                selectedCell = null
                isMoveStarted = false
                switchCurrentTeam()
            }
        }
    }

    private fun movePiece(selectedCell: Cell, cell: Cell) {
        if (cell.piece == null) {
            cell.piece = selectedCell.piece
            selectedCell.piece = null
        } else {
            cell.piece = selectedCell.piece
            selectedCell.piece = null
            if (currentTeam == WHITE) {
                blackCells.remove(cell)
            } else {
                whiteCells.remove(cell)
            }
        }
        if (cell.piece is Pawn) {
            checkForPromotion(cell)
        }
        cell.piece!!.setIsMoved()
        if (currentTeam == WHITE) {
            Collections.replaceAll(whiteCells, selectedCell, cell)
        } else {
            Collections.replaceAll(blackCells, selectedCell, cell)
        }
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
}
