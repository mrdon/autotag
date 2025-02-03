package com.example.ftpsclient.service

import com.example.ftpsclient.data.FtpsServer
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPSClient
import org.apache.commons.net.util.TrustManagerUtils
import java.io.FileDescriptor
import java.io.FileInputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

class FtpsService() {
    private val ftpsClient: FTPSClient

    init {
        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, arrayOf<TrustManager>(TrustManagerUtils.getAcceptAllTrustManager()), null)
        }
        this.ftpsClient = FTPSClient(false, sslContext)
    }

    fun connect(server: FtpsServer): Boolean {
        return try {
            println("connecting on ${server.port}")
            ftpsClient.connect(server.hostname, server.port)
            println("logging in")
            ftpsClient.login(server.username, server.password)
            println("logged in")
            ftpsClient.enterLocalPassiveMode()
            ftpsClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpsClient.changeWorkingDirectory(server.directory)
            println("Somehow it worked???")
            true
        } catch (e: Exception) {
            println("Unable to connect to server: $e")
            false
        }
    }

    fun uploadFile(fileName: String, fileDescriptor: FileDescriptor): Boolean {
        return try {
            val fileInputStream = FileInputStream(fileDescriptor)
            ftpsClient.storeFile(fileName, fileInputStream)
            fileInputStream.close()
            true
        } catch (e: Exception) {
            println("Unable to upload file to server: $e")
            false
        }
    }

    fun testConnection(server: FtpsServer): Boolean {
        return try {
            if (!connect(server)) {
                return false
            }
            println("how the fuck are we here?")
            disconnect()
            true
        } catch (e: Exception) {
            println("Unable to connect to server: $e")
            false
        }
    }

    fun disconnect() {
        if (ftpsClient.isConnected) {
            ftpsClient.disconnect()
        }
    }
}
