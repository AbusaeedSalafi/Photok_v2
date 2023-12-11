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

package com.lockmedia.applock.videoplayer.ui

import android.app.Application
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.models.database.entity.Photo
import com.lockmedia.applock.models.repositories.PhotoRepository
import com.lockmedia.applock.other.onMain
import com.lockmedia.applock.securityapp.EncryptionManager
import com.lockmedia.applock.uiunits.bindings.ObservableViewModel
import com.lockmedia.applock.videoplayer.data.AesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager
) : ObservableViewModel(app) {

    @get:Bindable
    var player: SimpleExoPlayer? = null
        set(value) {
            field = value
            notifyChange(com.lockmedia.applock.BR.player, value)
        }

    /**
     * Create and prepare the [player] to play the passed video.
     */
    fun setupPlayer(photoId: Int) {
        releasePlayer()

        viewModelScope.launch(Dispatchers.IO) {
            val photo = photoRepository.get(photoId)

            player = SimpleExoPlayer.Builder(app)
                .setMediaSourceFactory(createMediaSourceFactory())
                .build()
                .apply {
                    onMain {
                        setMediaItem(createMediaItem(photo))
                        prepare()
                        playWhenReady = true
                    }
                }
        }
    }

    private fun createMediaSourceFactory(): MediaSourceFactory {
        val aesDataSource = AesDataSource(encryptionManager)

        val factory = DataSource.Factory {
            aesDataSource
        }

        return ProgressiveMediaSource.Factory(factory)
    }

    private fun createMediaItem(photo: Photo): MediaItem {
        val uri = Uri.fromFile(app.getFileStreamPath(photo.internalFileName).canonicalFile)

        return MediaItem.Builder()
            .setMimeType(photo.type.mimeType)
            .setUri(uri)
            .build()
    }

    /**
     * Release the current player
     */
    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}