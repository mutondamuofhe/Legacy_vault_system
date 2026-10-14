package com.example.legacyvaultsystem

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CurrencyApiServiceTest {

    @Test
    fun testFetchLatestRates_ReturnsValidData() {
        val latch = CountDownLatch(1)
        var resultRates: CurrencyRates? = null

        CurrencyApiService.fetchLatestRates { rates ->
            resultRates = rates
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)

        assertNotNull(resultRates)
        val rates = resultRates!!
        assertTrue(rates.zar > 0)
        assertTrue(rates.eur > 0)
        assertTrue(rates.gbp > 0)
    }
}