package com.example.paxradio.data

sealed class PlayerState {
    object Idle : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Error : PlayerState()
    object NoStream : PlayerState()
}

