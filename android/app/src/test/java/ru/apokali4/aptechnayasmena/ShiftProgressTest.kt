package ru.apokali4.aptechnayasmena

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftProgressTest {
    @Test fun correctActionAdvancesToNextStation() {
        val progress = ShiftProgress()
        val result = progress.choose(ShiftFlow.steps.first().correctIndex)
        assertTrue(result.correct)
        assertEquals(ShiftStage.SHELF, result.nextStage)
    }

    @Test fun wrongActionDoesNotAdvanceTheShift() {
        val progress = ShiftProgress()
        val result = progress.choose((ShiftFlow.steps.first().correctIndex + 1) % ShiftFlow.steps.first().options.size)
        assertFalse(result.correct)
        assertEquals(ShiftStage.DELIVERY, result.nextStage)
    }

    @Test fun completingAllStationsFinishesTheShift() {
        val progress = ShiftProgress()
        ShiftFlow.steps.forEach { progress.choose(it.correctIndex) }
        assertEquals(ShiftStage.COMPLETE, progress.stage)
        assertEquals(3, progress.completedCount)
    }

    @Test fun invalidOptionDoesNotAdvanceTheShift() {
        val progress = ShiftProgress()
        val result = progress.choose(-1)
        assertFalse(result.correct)
        assertEquals(ShiftStage.DELIVERY, progress.stage)
    }
}
