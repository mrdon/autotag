package com.example.ftpsclient.service

import com.example.ftpsclient.data.FtpsServer
import org.apache.commons.net.ProtocolCommandEvent
import org.apache.commons.net.ProtocolCommandListener
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
//        this.ftpsClient.addProtocolCommandListener (object :
//            ProtocolCommandListener {
//            override fun protocolCommandSent(protocolCommandEvent: ProtocolCommandEvent) {
//                System.out.printf(
//                    "[%s][%d] Command sent : [%s]-%s", Thread.currentThread().name,
//                    System.currentTimeMillis(), protocolCommandEvent.command,
//                    protocolCommandEvent.message
//                )
//            }
//
//            override fun protocolReplyReceived(protocolCommandEvent: ProtocolCommandEvent) {
//                System.out.printf(
//                    "[%s][%d] Reply received : %s", Thread.currentThread().name,
//                    System.currentTimeMillis(), protocolCommandEvent.message
//                )
//            }
//        })
    }

    fun connect(server: FtpsServer): Boolean {
        return try {
            println("connecting on ${server.port}")
            ftpsClient.connect(server.hostname, server.port)
            println("logging in")
            ftpsClient.login(server.username, server.password)
            println("logged in")
            ftpsClient.execPBSZ(0)
            ftpsClient.execPROT("P")
            ftpsClient.enterLocalPassiveMode()
            ftpsClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpsClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE)
            ftpsClient.changeWorkingDirectory(server.directory)
            true
        } catch (e: Exception) {
            println("Unable to connect to server: $e")
            false
        }
    }

    fun uploadFile(fileName: String, fileDescriptor: FileDescriptor): Boolean {
        return try {
            val fileInputStream = FileInputStream(fileDescriptor)
            println("Uploading file: $fileName, $fileDescriptor")
            ftpsClient.enterLocalPassiveMode()
            ftpsClient.setFileType(FTP.BINARY_FILE_TYPE)
            val success = ftpsClient.storeFile("/$fileName", fileInputStream)
            fileInputStream.close()
            println("File upload done - $success")
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
