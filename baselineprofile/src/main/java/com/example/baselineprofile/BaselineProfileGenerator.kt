package com.example.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile Generator for Android TV.
 * Walks through critical user journeys to pre-compile them and remove JIT jank.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.example.tvapp",
        includeInStartupProfile = true // Pre-compile startup for faster boot
    ) {
        // 1. App Startup Journey
        pressHome()
        startActivityAndWait()

        // 2. Navigation & Scrolling Journey (Critical for TV)
        // Wait for any scrollable container (LazyColumn or LazyRow)
        device.wait(Until.hasObject(By.scrollable(true)), 10_000)

        // Robust Scrolling: Re-find the object in each loop to avoid StaleObjectException
        repeat(3) {
            device.findObject(By.scrollable(true))?.let { list ->
                list.fling(Direction.DOWN)
                device.waitForIdle()
                Thread.sleep(500) // Small delay for cinematic transitions to settle
            }
        }
        
        repeat(3) {
            device.findObject(By.scrollable(true))?.let { list ->
                list.fling(Direction.UP)
                device.waitForIdle()
                Thread.sleep(500)
            }
        }

        // 3. Horizontal Row Interaction
        // Try to find a horizontal scrollable row specifically if possible
        repeat(2) {
             device.findObject(By.scrollable(true))?.let { list ->
                list.fling(Direction.RIGHT)
                device.waitForIdle()
                Thread.sleep(500)
            }
        }
    }
}
