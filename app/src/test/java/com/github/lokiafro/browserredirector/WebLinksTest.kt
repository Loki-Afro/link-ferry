package com.github.lokiafro.browserredirector

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebLinksTest {
    @Test
    fun acceptsWebLinks() {
        listOf(
            "http://example.com",
            "https://example.com:443/a%2Fb?x=%2B&x=2#part",
            "https://[2001:db8::1]:8080/",
            "https://bücher.example/über",
            "https://user:p%40ss@bücher.example:443/"
        ).forEach { assertTrue(it, WebLinks.isSupported(it)) }
    }

    @Test
    fun rejectsMalformedAndUnsupportedInput() {
        listOf(
            "", "/relative", "https:example.com", "https:///example.com", "HTTPS://example.com/a",
            "https://example.com:/", "https://example.com:65536/", "https://bücher.example:x",
            "https://example.com/%zz", "https://example.com/two words",
            "https://good.example\\@bad.example/", "https://user@name@bücher.example/",
            "intent://example.com", "file:///sdcard/a", "javascript:alert(1)", "mailto:a@example.com"
        ).forEach { assertFalse(it, WebLinks.isSupported(it)) }
    }
}
