package com.example.chessapp

abstract class Piece(color: String) {
    private var isMoved = false
    val color = color

    open fun getDrawableID(): Int {
        return 0
    }

    fun setIsMoved() {
        isMoved = true
    }

    fun getIsMoved(): Boolean = isMoved
}
