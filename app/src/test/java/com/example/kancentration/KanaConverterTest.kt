package com.example.kancentration

import com.example.kancentration.util.KanaConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class KanaConverterTest {

    @Test
    fun testOnReadingsConversion() {
        assertEquals("nichi", KanaConverter.toRomaji("ニチ"))
        assertEquals("jitsu", KanaConverter.toRomaji("ジツ"))
        assertEquals("etsu", KanaConverter.toRomaji("エツ"))
        assertEquals("toku", KanaConverter.toRomaji("トク"))
        assertEquals("jin", KanaConverter.toRomaji("ジン"))
        assertEquals("nin", KanaConverter.toRomaji("ニン"))
        assertEquals("nyû", KanaConverter.toRomaji("ニュウ"))
        assertEquals("shi", KanaConverter.toRomaji("シ"))
        assertEquals("matsu", KanaConverter.toRomaji("マツ"))
        assertEquals("batsu", KanaConverter.toRomaji("バツ"))
        assertEquals("yû", KanaConverter.toRomaji("ユウ"))
        assertEquals("sha", KanaConverter.toRomaji("シャ"))
        assertEquals("chô", KanaConverter.toRomaji("チョウ"))
        assertEquals("tô", KanaConverter.toRomaji("トウ"))
        assertEquals("byaku", KanaConverter.toRomaji("ビャク"))
        assertEquals("hyaku", KanaConverter.toRomaji("ヒャク"))
        assertEquals("gyû", KanaConverter.toRomaji("ギュウ"))
        assertEquals("ô", KanaConverter.toRomaji("オウ"))
    }

    @Test
    fun testKunReadingsConversion() {
        assertEquals("hi", KanaConverter.toRomaji("ひ"))
        assertEquals("-bi", KanaConverter.toRomaji("-び"))
        assertEquals("-ka", KanaConverter.toRomaji("-か"))
        assertEquals("iwaku", KanaConverter.toRomaji("いわく"))
        assertEquals("mo.tsu", KanaConverter.toRomaji("も.つ"))
        assertEquals("mo.teru", KanaConverter.toRomaji("も.てる"))
        assertEquals("tsuchi", KanaConverter.toRomaji("つち"))
        assertEquals("samurai", KanaConverter.toRomaji("さむらい"))
        assertEquals("ima.da", KanaConverter.toRomaji("いま.だ"))
        assertEquals("yaiba", KanaConverter.toRomaji("やいば"))
        assertEquals("ama-", KanaConverter.toRomaji("あま-"))
    }
}
