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
            "Pawn" -> {}
            "Rook" -> {}
            "Knight" -> {}
            "Bishop" -> {}
            "Queen" -> {}
            "King" -> {}
        }
        return possibleMoves
    }
}
