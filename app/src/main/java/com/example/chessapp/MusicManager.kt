package com.example.chessapp

import android.content.Context
import android.media.MediaPlayer
import kotlin.random.Random

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var musicMuted = false
    private var musicVolume = 100

    fun setMusicMuted(muted: Boolean) {
        musicMuted = muted
        updateVolume()
    }

    fun setMusicVolume(volume: Int) {
        musicVolume = volume
        updateVolume()
    }

    private fun updateVolume() {
        mediaPlayer?.let {
            val volume = if (musicMuted) 0 else musicVolume
            val volumeFloat = volume.toFloat() / 100
            it.setVolume(volumeFloat, volumeFloat)
        }
    }

    fun playInGameMusic() {
        stopMusic()

        val musicId = getInGameMusicResourceId()
        if (musicId != 0) {
            mediaPlayer = MediaPlayer.create(context, musicId)
            mediaPlayer?.setOnCompletionListener {
                // New music is called after the old one has finished playing
                playInGameMusic() // Play a new random track
            }
            mediaPlayer?.isLooping = false // Tracks will now not always loop
            updateVolume() // Update volume before playback
            mediaPlayer?.start()
        }
    }

    fun playEndGameMusic() {
        stopMusic()

        val musicId = getEndGameMusicResourceId()
        if (musicId != 0) {
            mediaPlayer = MediaPlayer.create(context, musicId)
            mediaPlayer?.isLooping = true
            updateVolume() // Update volume before playback
            mediaPlayer?.start()
        }
    }

    fun playMenuMusic() {
        stopMusic()

        val musicId = getMenuMusicResourceId()
        if (musicId != 0) {
            mediaPlayer = MediaPlayer.create(context, musicId)
            mediaPlayer?.isLooping = true
            updateVolume() // Update volume before playback
            mediaPlayer?.start()
        }
    }

    fun stopMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun resumeMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    private fun getInGameMusicResourceId(): Int {
        return when (Random.nextInt(1, 4)) {
            1 -> R.raw.in_game_music_1
            2 -> R.raw.in_game_music_2
            else -> R.raw.in_game_music_3
        }
    }

    private fun getEndGameMusicResourceId(): Int {
        return R.raw.end_game_music
    }

    private fun getMenuMusicResourceId(): Int {
        return R.raw.menu_music
    }
}
