package com.example.chessapp

class Queen(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_queen
            else -> R.drawable.black_queen
        }
    }
}
