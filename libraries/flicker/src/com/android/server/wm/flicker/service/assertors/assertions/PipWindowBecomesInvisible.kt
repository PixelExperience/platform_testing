/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.wm.flicker.service.assertors.assertions

import com.android.server.wm.flicker.service.IScenarioInstance
import com.android.server.wm.flicker.service.assertors.ComponentTemplate
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTraceSubject

/**
 * Checks that [getWindowState] window is pinned and visible at the start and then becomes unpinned
 * and invisible at the same moment, and remains unpinned and invisible until the end of the
 * transition
 */
class PipWindowBecomesInvisible(component: ComponentTemplate) :
    AssertionTemplateWithComponent(component) {
    /** {@inheritDoc} */
    override fun doEvaluate(
        scenarioInstance: IScenarioInstance,
        wmSubject: WindowManagerTraceSubject
    ) {
        val appComponent = component
        wmSubject
            .invoke("hasPipWindow") {
                it.isPinned(appComponent.build(scenarioInstance))
                    .isAppWindowVisible(appComponent.build(scenarioInstance))
            }
            .then()
            .invoke("!hasPipWindow") {
                it.isNotPinned(appComponent.build(scenarioInstance))
                    .isAppWindowInvisible(appComponent.build(scenarioInstance))
            }
            .forAllEntries()
    }
}
