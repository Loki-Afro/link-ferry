package com.github.lokiafro.browserredirector

import java.net.IDN
import java.net.URI
import java.net.URISyntaxException

internal object WebLinks {
    fun isSupported(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (_: URISyntaxException) {
            return false
        }

        val scheme = uri.scheme ?: return false
        if (scheme != "http" && scheme != "https") return false

        val authority = uri.rawAuthority ?: return false
        if (uri.host != null) return validPort(authority.substringAfterLast('@'), uri.port)

        // URI.host uses ASCII DNS syntax. Validate internationalized hosts while
        // still handing the original URI, including its spelling and escapes, to the browser.
        val hostAndPort = authority.substringAfterLast('@')
        val host = hostAndPort.substringBefore(':')
        if (host.all { it.code < 128 } || authority.count { it == '@' } > 1) return false

        return try {
            val asciiHost = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES)
            val portSuffix = hostAndPort.removePrefix(host)
            val asciiUri = URI("https://$asciiHost$portSuffix").parseServerAuthority()
            asciiUri.host != null && validPort(asciiHost + portSuffix, asciiUri.port)
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: URISyntaxException) {
            false
        }
    }

    private fun validPort(hostAndPort: String, port: Int): Boolean =
        !hostAndPort.endsWith(':') && port in -1..65535
}
