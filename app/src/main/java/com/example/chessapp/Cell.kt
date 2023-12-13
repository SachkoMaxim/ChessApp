package com.example.chessapp

import android.util.Log
import android.widget.Button

class Cell(button: Button, piece: Piece?, board: ChessBoard) {
    val button = button
    var piece = piece
    val board = board
    private val cells = board.getCells()
    private var x: Int? = null
    private var y: Int? = null

    fun setCoords(xCoord: Int, yCoord: Int) {
        if (x == null && y == null) {
            x = xCoord
            y = yCoord
        } else {
            Log.e("Cell", "x and y already assigned")
        }
    }

    fun getX() = x
    fun getY() = y

    fun getPossibleMoves(isCheckingForMate: Boolean = false): MutableList<Pair<Int, Int>> {
        val possibleMoves = mutableListOf<Pair<Int, Int>>()
        if (piece == null) {
            return possibleMoves
        }
        when (piece!!::class.java.simpleName) {
            "Pawn" -> {
                val direction = if (piece!!.color == ChessBoard.WHITE) 1 else -1

                // Перевірка можливості руху вперед на одну клітинку
                if (coordsInRange(x!! + direction, y!!) && cells[x!! + direction][y!!]!!.piece == null) {
                    possibleMoves.add(Pair(x!! + direction, y!!))

                    // Перевірка можливості подвійного ходу для пішака на початку гри
                    if (!piece!!.getIsMoved() && cells[x!! + 2 * direction][y!!]!!.piece == null) {
                        possibleMoves.add(Pair(x!! + 2 * direction, y!!))
                    }
                }

                // Перевірка можливості атаки по діагоналі
                if (coordsInRange(x!! + direction, y!! + 1) &&
                    cells[x!! + direction][y!! + 1]!!.piece != null &&
                    cells[x!! + direction][y!! + 1]!!.piece?.color != piece!!.color
                ) {
                    possibleMoves.add(Pair(x!! + direction, y!! + 1))
                }

                if (coordsInRange(x!! + direction, y!! - 1) &&
                    cells[x!! + direction][y!! - 1]!!.piece != null &&
                    cells[x!! + direction][y!! - 1]!!.piece?.color != piece!!.color
                ) {
                    possibleMoves.add(Pair(x!! + direction, y!! - 1))
                }
            }
            "Rook" -> {}
            "Knight" -> {}
            "Bishop" -> {}
            "Queen" -> {}
            "King" -> {}
        }
        return possibleMoves
    }

    private fun coordsInRange(x: Int, y: Int): Boolean {
        return (x in 0..7 && y in 0..7)
    }
}
