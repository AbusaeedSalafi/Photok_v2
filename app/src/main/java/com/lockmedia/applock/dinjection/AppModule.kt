/*
 *   Copyright 2020-2023 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.lockmedia.applock.dinjection

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.lockmedia.applock.gallerymedia.ui.importing.SharedUrisStore
import com.lockmedia.applock.models.database.PhotokDatabase
import com.lockmedia.applock.models.database.PhotokDatabase.Companion.DATABASE_NAME
import com.lockmedia.applock.securityapp.EncryptionManager
import com.lockmedia.applock.settings.data.Config
import javax.inject.Singleton

/**
 * Hilt Module for [SingletonComponent].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotokDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        PhotokDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun providePhotoDao(database: PhotokDatabase) = database.getPhotoDao()

    @Provides
    @Singleton
    fun provideConfig(@ApplicationContext app: Context) = Config(app)

    @Provides
    @Singleton
    fun provideEncryptionManager() = EncryptionManager()

    @Provides
    @Singleton
    fun provideSharedUrisStore() = SharedUrisStore()
}