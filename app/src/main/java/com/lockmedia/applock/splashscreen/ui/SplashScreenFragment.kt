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

package com.lockmedia.applock.splashscreen.ui

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.lockmedia.applock.R

/**
 * Fragment to display a splash screen and check application state.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.addOnPropertyChange<AppStartState>(com.lockmedia.applock.BR.appStartState) {
            when (it) {
                AppStartState.FIRST_START -> {
                    // Delay for 3 seconds (3000 milliseconds) before navigating to onboarding
                    Handler().postDelayed({
                        navigate(R.id.action_splashScreenFragment_to_onBoardingFragment)
                    }, 2000)
                }
                AppStartState.SETUP -> {
                    // Delay for 3 seconds (3000 milliseconds) before navigating to setup
                    Handler().postDelayed({
                        navigate(R.id.action_splashScreenFragment_to_setupFragment)
                    }, 2000)
                }
                AppStartState.LOCKED -> {
                    // Delay for 3 seconds (3000 milliseconds) before navigating to unlock
                    Handler().postDelayed({
                        navigate(R.id.action_splashScreenFragment_to_unlockFragment)
                    }, 2000)
                }
            }
        }
        viewModel.checkApplicationState()

    }


    private fun navigate(fragment: Int) {
        findNavController().navigate(fragment)
    }
}