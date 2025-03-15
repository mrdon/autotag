package com.example.ftpsclient.service

import java.io.IOException
import java.io.InputStream

class ProcessInputStream(private val `in`: InputStream) :
    InputStream() {
    var sumRead = 0

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        val readCount = `in`.read(b)
        evaluatePercent(readCount.toLong())
        return readCount
    }


    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val readCount = `in`.read(b, off, len)
        println("Read: $readCount")
        evaluatePercent(readCount.toLong())
        return readCount
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skip = `in`.skip(n)
        evaluatePercent(skip)
        return skip
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val read = `in`.read()
        if (read != -1) {
            evaluatePercent(1)
        }
        return read
    }


    private fun evaluatePercent(readCount: Long) {
        if (readCount != -1L) {
            sumRead += readCount.toInt()
        }
    }

}