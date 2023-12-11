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

package com.lockmedia.applock.backupData.ui

import android.app.Application
import android.net.Uri
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.backupData.data.BackupMetaData
import com.lockmedia.applock.models.database.entity.Photo
import com.lockmedia.applock.models.io.EncryptedStorageManager
import com.lockmedia.applock.models.repositories.PhotoRepository
import com.lockmedia.applock.other.createGson
import com.lockmedia.applock.other.extensions.lazyClose
import com.lockmedia.applock.settings.data.Config
import com.lockmedia.applock.uiunits.base.processdialogs.BaseProcessViewModel
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * ViewModel to create a backup.
 * Backups photos and meta data to zip file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val config: Config
) : BaseProcessViewModel<Photo>(app) {

    lateinit var uri: Uri
    private lateinit var zipOutputStream: ZipOutputStream
    private var backedUpPhotos = arrayListOf<Photo>()
    private val gson: Gson = createGson()

    override suspend fun preProcess() {
        items = photoRepository.getAll()
        elementsToProcess = items.size
        openZipFile()
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val success = writePhotoToZipEntry(item)
        if (success) {
            backedUpPhotos.add(item)
        } else {
            failuresOccurred = true
        }
    }

    override suspend fun postProcess() {
        val details = BackupMetaData(config.securityPassword!!, backedUpPhotos)
        val metaBytes = gson.toJson(details).toByteArray()
        writeZipEntry(BackupMetaData.FILE_NAME, ByteArrayInputStream(metaBytes))

        zipOutputStream.lazyClose()
        super.postProcess()
    }

    private fun openZipFile() {
        val out = app.contentResolver.openOutputStream(uri)
        zipOutputStream = ZipOutputStream(out)
    }

    private fun writePhotoToZipEntry(photo: Photo): Boolean {
        val input = encryptedStorageManager.internalOpenFileInput(photo.internalFileName)
        val fileSuccess = writeZipEntry(photo.internalFileName, input)
        input?.lazyClose()

        val thumbnailInput =
            encryptedStorageManager.internalOpenFileInput(photo.internalThumbnailFileName)
        val thumbnailSuccess = writeZipEntry(photo.internalThumbnailFileName, thumbnailInput)
        thumbnailInput?.lazyClose()

        val videoPreviewSuccess = if (photo.type.isVideo) {
            val videoPreviewInput =
                encryptedStorageManager.internalOpenFileInput(photo.internalVideoPreviewFileName)
            val success = writeZipEntry(photo.internalVideoPreviewFileName, videoPreviewInput)
            videoPreviewInput?.lazyClose()
            success
        } else {
            true // Just set true, since it can be ignored
        }

        return fileSuccess && thumbnailSuccess && videoPreviewSuccess
    }

    private fun writeZipEntry(fileName: String, inputStream: InputStream?): Boolean {
        inputStream ?: return false

        return try {
            val entry = ZipEntry(fileName)
            zipOutputStream.putNextEntry(entry)
            inputStream.copyTo(zipOutputStream)
            zipOutputStream.closeEntry()
            true
        } catch (e: IOException) {
            Timber.d("Cloud not write to backup: $e")
            false
        }
    }
}
