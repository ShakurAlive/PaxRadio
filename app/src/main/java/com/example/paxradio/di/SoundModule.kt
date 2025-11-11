package com.example.paxradio.di

import android.content.Context
import com.example.paxradio.player.sound.SoundPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SoundModule {

    @Provides
    @Singleton
    fun provideSoundPlayer(
        @ApplicationContext context: Context
    ): SoundPlayer {
        return SoundPlayer(context)
    }
}
