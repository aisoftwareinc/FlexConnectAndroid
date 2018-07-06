package com.aisoftware.flexconnect.util

import android.graphics.Bitmap
import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.model.Delivery
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@SmallTest
class ConverterUtilTest {

    @Mock private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testFormatExtendedAddress() {
        val delivery = getDelivery()
        val expected = "Phoenix, AZ 85086"
        val actual = ConverterUtil.formatExtendedAddress(delivery)
        assertNotNull(actual)
        assertEquals("Unexpected format result", expected, actual)
    }

    @Test
    fun testFormatExtendedAddressMinimal() {
        val delivery = Delivery(1L)
        val expected = ","
        val actual = ConverterUtil.formatExtendedAddress(delivery).trim()
        assertNotNull(actual)
        assertEquals("Unexpected format result", expected, actual)
    }

    @Test
    @Ignore
    fun testConvertImageMockedBitmap() {
        val expected = ""
        val actual = ConverterUtil.convertImage(bitmap)
        assertNotNull(actual)
        assertEquals("Unexpected converted value", expected, actual)
    }

    private fun getDelivery(): Delivery
        = Delivery(
                1L,
                "guid",
            "5",
                "Status",
                "May 1 1985",
                "12:00 pm",
                "FlexConnect",
                "fc@email.com",
                "9999999999",
                "123 Main St",
                "Ste 1",
                "Phoenix",
                "AZ",
                "85086",
                "1",
                "2",
                "30 minutes",
                "12 miles",
                "These dudes are for real.")
}