package com.example.marcadechoferes.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MyModule {
    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext



}