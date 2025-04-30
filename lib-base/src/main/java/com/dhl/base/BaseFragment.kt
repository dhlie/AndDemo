/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dhl.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dhl.base.utils.log

open class BaseFragment : Fragment() {

    init {
        log("Lifecycle -> newInstance - $this")
    }

    fun log(message: String) {
        val tag = this::class.java.simpleName
        log { message }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("Lifecycle -> onCreate savedInstanceState:$savedInstanceState - $this")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log("Lifecycle -> onAttach - $this")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        log("Lifecycle -> onCreateView - $this")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("Lifecycle -> onViewCreated - $this")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        log("Lifecycle -> onActivityCreated - $this")
    }

    override fun onPause() {
        super.onPause()
        log("Lifecycle -> onPause - $this")
    }

    override fun onStart() {
        super.onStart()
        log("Lifecycle -> onStart - $this")
    }

    override fun onResume() {
        super.onResume()
        log("Lifecycle -> onResume - $this")
    }

    override fun onStop() {
        super.onStop()
        log("Lifecycle -> onStop - $this")
    }

    override fun onDetach() {
        super.onDetach()
        log("Lifecycle -> onDetach - $this")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log("Lifecycle -> onDestroyView - $this")
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        log("Lifecycle -> onAttachFragment:$childFragment - $this")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        log("Lifecycle -> onConfigurationChanged - $this")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        log("Lifecycle -> onHiddenChanged - $this")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("Lifecycle -> onDestroy - $this")
    }

}
