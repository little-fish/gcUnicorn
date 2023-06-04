/*
 * gcUnicorn
 * Copyright (C) 2023  Martin Misiarz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cz.babi.gcunicorn.android.network

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


/**
 * Custom SSL socket factory which enables TLS v1.2 when creating SSLSockets.
 *
 * For some reason, android supports TLS v1.2 from API 16, but enables it by default only from API 20.
 *
 * [https://developer.android.com/reference/javax/net/ssl/SSLSocket.html](https://developer.android.com/reference/javax/net/ssl/SSLSocket.html)
 * [https://github.com/square/okhttp/issues/2372#issuecomment-244807676](https://github.com/square/okhttp/issues/2372#issuecomment-244807676)
 * [https://www.ssllabs.com/ssltest/](https://www.ssllabs.com/ssltest/)
 *
 * @param sslSocketFactory Underlying SSL socket factory.
 * @see [SSLSocketFactory]
 *
 * @author Alex Gotev
 * @version 1.0.0
 * @since 1.0.0
 */
class Tls12SocketFactory(sslSocketFactory: SSLSocketFactory) : SSLSocketFactory() {

    private val delegate = sslSocketFactory

    companion object {
        const val TLS_V12 = "TLSv1.2"
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return patch(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    /**
     * Enables [TLS_V12] by default.
     * @param s Socket to be patched.
     * @return Patched socket.
     */
    private fun patch(s: Socket): Socket {
        if (s is SSLSocket) {
            s.enabledProtocols = arrayOf(TLS_V12)
        }

        return s
    }
}