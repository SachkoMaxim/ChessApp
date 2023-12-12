package com.example.chessapp

class King(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_king
            else -> R.drawable.black_king
        }
    }
}
