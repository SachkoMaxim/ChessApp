package com.example.chessapp

class Bishop(color: String) : Piece(color) {
    override fun getDrawableID(): Int {
        return when (color) {
            ChessBoard.WHITE -> R.drawable.white_bishop
            else -> R.drawable.black_bishop
        }
    }
}
