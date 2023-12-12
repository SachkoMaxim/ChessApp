package com.example.chessapp

class Rook(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_rook
            else -> R.drawable.black_rook
        }
    }
}
