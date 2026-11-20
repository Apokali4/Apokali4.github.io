package ru.apokali4.aptechnayasmena

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionTest {
    @Test fun startsAtLevelOne() { assertEquals(1, levelForXp(0)) }
    @Test fun advancesEveryHundredXp() { assertEquals(2, levelForXp(100)); assertEquals(3, levelForXp(200)) }
    @Test fun negativeXpCannotLowerLevelBelowOne() { assertEquals(1, levelForXp(-50)) }
}
