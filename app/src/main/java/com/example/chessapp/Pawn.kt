package com.example.chessapp

class Pawn(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_pawn
            else -> R.drawable.black_pawn
        }
    }
}
