package com.example.chessapp

class Knight(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_knight
            else -> R.drawable.black_knight
        }
    }
}
