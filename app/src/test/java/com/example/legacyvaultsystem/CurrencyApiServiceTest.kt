package com.example.legacyvaultsystem

import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyApiServiceTest {

    @Test
    fun testCurrencyRatesFallback() {
        val fallback = CurrencyRates("USD", 18.50, 0.92, 0.78, 1.52, 1.38, "Offline", false)
        assertTrue(fallback.zar > 0)
        assertTrue(fallback.eur > 0)
    }
}