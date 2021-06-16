package io.github.gcdd1993.cas.client.app1.config

import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.ssl.SSLContexts
import org.jasig.cas.client.ssl.HttpURLConnectionFactory
import java.net.HttpURLConnection
import java.net.URLConnection
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

class InsecureHttpsURLConnectionFactory : HttpURLConnectionFactory {
    private val sslContext = trustAllSSLContext()
    override fun buildHttpURLConnection(conn: URLConnection): HttpURLConnection {
        if (conn is HttpsURLConnection) {
            conn.sslSocketFactory = sslContext.socketFactory
            conn.hostnameVerifier = NoopHostnameVerifier.INSTANCE
        }
        return conn as HttpURLConnection
    }

    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
    fun trustAllSSLContext(): SSLContext {
        val acceptingTrustStrategy = TrustStrategy { _, _ -> true }
        return SSLContexts
            .custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            .build()
    }
}