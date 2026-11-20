package ru.apokali4.aptechnayasmena

fun levelForXp(xp: Int): Int = (xp.coerceAtLeast(0) / 100) + 1
