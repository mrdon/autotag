package com.example.ftpsclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FtpsServer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val hostname: String,
    val port: Int,
    val username: String,
    val password: String,
    val directory: String,

    val lastModified: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
