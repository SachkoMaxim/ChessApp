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

                // Checking the ability to move forward one cell
                addForwardMove(possibleMoves, direction, 1)

                // Checking if a pawn can move forward two cells at the start of the game and if there is no other piece in front of it
                if (!piece!!.getIsMoved() && cells[x!! + direction][y!!]!!.piece == null) {
                    addForwardMove(possibleMoves, direction, 2)
                }

                // Checking the possibility of a diagonal attack
                addDiagonalAttack(possibleMoves, direction, 1)
                addDiagonalAttack(possibleMoves, direction, -1)
            }

            "Rook" -> {
                addHorizAndVertOrDiagMoves(possibleMoves, 1, 0)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, 0)
                addHorizAndVertOrDiagMoves(possibleMoves, 0, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, 0, -1)
            }

            "Knight" -> {
                addKnightOrKingMove(possibleMoves, 1, 2)
                addKnightOrKingMove(possibleMoves, 1, -2)
                addKnightOrKingMove(possibleMoves, -1, 2)
                addKnightOrKingMove(possibleMoves, -1, -2)
                addKnightOrKingMove(possibleMoves, 2, 1)
                addKnightOrKingMove(possibleMoves, 2, -1)
                addKnightOrKingMove(possibleMoves, -2, 1)
                addKnightOrKingMove(possibleMoves, -2, -1)
            }

            "Bishop" -> {
                addHorizAndVertOrDiagMoves(possibleMoves, 1, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, 1, -1)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, -1)
            }

            "Queen" -> {
                addHorizAndVertOrDiagMoves(possibleMoves, 1, 0)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, 0)
                addHorizAndVertOrDiagMoves(possibleMoves, 0, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, 0, -1)
                addHorizAndVertOrDiagMoves(possibleMoves, 1, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, 1, -1)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, 1)
                addHorizAndVertOrDiagMoves(possibleMoves, -1, -1)
            }

            "King" -> {
                addKnightOrKingMove(possibleMoves, 1, 0)
                addKnightOrKingMove(possibleMoves, -1, 0)
                addKnightOrKingMove(possibleMoves, 0, 1)
                addKnightOrKingMove(possibleMoves, 0, -1)
                addKnightOrKingMove(possibleMoves, 1, 1)
                addKnightOrKingMove(possibleMoves, 1, -1)
                addKnightOrKingMove(possibleMoves, -1, 1)
                addKnightOrKingMove(possibleMoves, -1, -1)
            }
        }
        return possibleMoves
    }

    private fun addForwardMove(
        possibleMoves: MutableList<Pair<Int, Int>>,
        direction: Int,
        steps: Int
    ) {
        val newX = x!! + direction * steps
        val newY = y!!

        if (coordsInRange(newX, newY) && cells[newX][newY]!!.piece == null) {
            possibleMoves.add(Pair(newX, newY))
        }
    }

    private fun addDiagonalAttack(
        possibleMoves: MutableList<Pair<Int, Int>>,
        direction: Int,
        offset: Int
    ) {
        val newX = x!! + direction
        val newY = y!! + offset

        if (coordsInRange(newX, newY) &&
            cells[newX][newY]!!.piece != null &&
            cells[newX][newY]!!.piece?.color != piece!!.color
        ) {
            possibleMoves.add(Pair(newX, newY))
        }
    }

    private fun addHorizAndVertOrDiagMoves(
        possibleMoves: MutableList<Pair<Int, Int>>,
        directionX: Int,
        directionY: Int
    ) {
        for (i in 1 until ChessBoard.BOARD_SIZE) {
            val newX = x!! + i * directionX
            val newY = y!! + i * directionY

            if (!coordsInRange(newX, newY)) {
                break
            }

            if (cells[newX][newY]!!.piece == null) {
                possibleMoves.add(Pair(newX, newY))
            } else if (cells[newX][newY]!!.piece?.color != piece!!.color) {
                possibleMoves.add(Pair(newX, newY))
                break // stop adding moves if a piece of a different color is found
            } else {
                break // stop adding moves if a piece of its color is found
            }
        }
    }

    private fun addKnightOrKingMove(
        possibleMoves: MutableList<Pair<Int, Int>>,
        directionX: Int,
        directionY: Int
    ) {
        val newX = x!! + directionX
        val newY = y!! + directionY

        if (coordsInRange(newX, newY) &&
            (cells[newX][newY]!!.piece == null ||
                    cells[newX][newY]!!.piece?.color != piece!!.color)
        ) {
            possibleMoves.add(Pair(newX, newY))
        }
    }

    private fun coordsInRange(x: Int, y: Int): Boolean {
        return (x in 0 until ChessBoard.BOARD_SIZE && y in 0 until ChessBoard.BOARD_SIZE)
    }

    fun promotePawn() {
        piece = Queen(piece!!.color)
    }
}
