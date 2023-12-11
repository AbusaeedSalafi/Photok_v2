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

package com.lockmedia.applock.forwarddialer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Forwarder Activity. Opens phone dialer and finishes.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ForwardDialerActivity : AppCompatActivity() {

    private val viewModel: ForwardDialerViewModel by viewModels()

    @Inject
    lateinit var navigator: ForwardDialerNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationEvent.observe(this) {
            navigator.navigate(it, this)
        }

        viewModel.evaluateNavigation()
    }
}