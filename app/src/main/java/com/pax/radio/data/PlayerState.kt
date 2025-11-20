package com.pax.radio.data

sealed class PlayerState {
    object Idle : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Buffering : PlayerState()
    object Error : PlayerState()
    object NoStream : PlayerState()
}

