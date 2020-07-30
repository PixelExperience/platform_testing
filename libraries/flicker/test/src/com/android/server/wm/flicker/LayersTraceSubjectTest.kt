/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.wm.flicker

import android.graphics.Region
import androidx.test.filters.FlakyTest
import com.android.server.wm.flicker.traces.layers.Layer
import com.android.server.wm.flicker.traces.layers.LayersTrace
import com.android.server.wm.flicker.traces.layers.LayersTrace.Companion.parseFrom
import com.android.server.wm.flicker.traces.layers.LayersTraceSubject
import com.android.server.wm.flicker.traces.layers.LayersTraceSubject.Companion.assertThat
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.nio.file.Paths

/**
 * Contains [LayersTraceSubject] tests. To run this test: `atest
 * FlickerLibTest:LayersTraceSubjectTest`
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LayersTraceSubjectTest {
    @Test
    fun testCanDetectEmptyRegionFromLayerTrace() {
        val layersTraceEntries = readLayerTraceFromFile("layers_trace_emptyregion.pb")
        try {
            assertThat(layersTraceEntries).coversRegion(DISPLAY_REGION).forAllEntries()
            Assert.fail("Assertion passed")
        } catch (e: AssertionError) {
            Truth.assertWithMessage("Contains path to trace")
                    .that(e.message)
                    .contains("layers_trace_emptyregion.pb")
            Truth.assertWithMessage("Contains timestamp").that(e.message).contains("0h38m28s8ms")
            Truth.assertWithMessage("Contains assertion function")
                    .that(e.message)
                    .contains("coversRegion")
            Truth.assertWithMessage("Contains debug info")
                    .that(e.message)
                    .contains("Region to test: $DISPLAY_REGION")
            Truth.assertWithMessage("Contains debug info")
                    .that(e.message)
                    .contains("first empty point: 0, 99")
        }
    }

    @FlakyTest
    @Test
    fun testCanDetectIncorrectVisibilityFromLayerTrace() {
        val layersTraceEntries = readLayerTraceFromFile("layers_trace_invalid_layer_visibility.pb")
        try {
            assertThat(layersTraceEntries)
                    .showsLayer("com.android.server.wm.flicker.testapp")
                    .then()
                    .hidesLayer("com.android.server.wm.flicker.testapp")
                    .forAllEntries()
            Assert.fail("Assertion passed")
        } catch (e: AssertionError) {
            Truth.assertWithMessage("Contains path to trace")
                    .that(e.message)
                    .contains("layers_trace_invalid_layer_visibility.pb")
            Truth.assertWithMessage("Contains timestamp")
                    .that(e.message)
                    .contains("2d22h13m14s303ms")
            Truth.assertWithMessage("Contains assertion function")
                    .that(e.message)
                    .contains("!isVisible")
            Truth.assertWithMessage("Contains debug info")
                    .that(e.message)
                    .contains("com.android.server.wm.flicker.testapp/com.android.server.wm.flicker.testapp"
                            + ".SimpleActivity#0 is visible")
        }
    }

    private fun detectRootLayer(fileName: String) {
        val layersTrace = readLayerTraceFromFile(fileName)
        for (entry in layersTrace.entries) {
            val flattened: List<Layer> = entry.flattenedLayers
            val rootLayers = flattened.filter { it.isRootLayer }
            Truth.assertWithMessage("Does not have any root layer")
                    .that(rootLayers.size)
                    .isGreaterThan(0)
            val firstParentId = rootLayers.first().parentId
            Truth.assertWithMessage("Has multiple root layers")
                    .that(rootLayers.all { it.parentId == firstParentId })
                    .isTrue()
        }
    }

    @Test
    fun testCanDetectRootLayer() {
        detectRootLayer("layers_trace_root.pb")
    }

    @Test
    fun testCanDetectRootLayerAOSP() {
        detectRootLayer("layers_trace_root_aosp.pb")
    }

    companion object {
        private val DISPLAY_REGION = Region(0, 0, 1440, 2880)
        private fun readLayerTraceFromFile(relativePath: String): LayersTrace {
            return try {
                parseFrom(readTestFile(relativePath), Paths.get(relativePath))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}