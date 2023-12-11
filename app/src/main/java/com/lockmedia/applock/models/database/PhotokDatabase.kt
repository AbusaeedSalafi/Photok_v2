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

package com.lockmedia.applock.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lockmedia.applock.models.database.PhotokDatabase.Companion.VERSION
import com.lockmedia.applock.models.database.dao.PhotoDao
import com.lockmedia.applock.models.database.entity.Photo

/**
 * Abstract Room Database.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Database(
    entities = [Photo::class],
    version = VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhotokDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "applock.db"
    }

    /**
     * Get the data access object for [Photo]
     */
    abstract fun getPhotoDao(): PhotoDao
}