package com.example.ftpsclient.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FtpsServer::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ftpsServerDao(): FtpsServerDao
}
