package com.pax.radio.di

import android.content.Context
import com.pax.radio.player.sound.SoundPlayer
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
