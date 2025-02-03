package com.example.ftpsclient.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FtpsServerDao {
    @Query("SELECT * FROM ftpsserver ORDER BY name ASC")
    suspend fun getAllServers(): List<FtpsServer>

    @Query("SELECT * FROM ftpsserver ORDER BY name ASC")
    fun getAllServersAsFlow(): Flow<List<FtpsServer>>

    @Query("SELECT * FROM ftpsserver WHERE id = :id")
    suspend fun getServerById(id: Int): FtpsServer?

    @Insert
    suspend fun insertServer(server: FtpsServer): Long

    @Update
    suspend fun updateServer(server: FtpsServer)

    @Delete
    suspend fun deleteServer(server: FtpsServer)

    @Query("DELETE FROM ftpsserver WHERE id = :id")
    suspend fun deleteServerById(id: Int)

    @Query("SELECT * FROM ftpsserver WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchServers(searchQuery: String): List<FtpsServer>

    @Query("SELECT COUNT(*) FROM ftpsserver")
    suspend fun getServerCount(): Int

    @Transaction
    suspend fun updateServerWithTimestamp(server: FtpsServer) {
        val timestamp = System.currentTimeMillis()
        val serverWithTimestamp = server.copy(lastModified = timestamp)
        updateServer(serverWithTimestamp)
    }

    @Query("SELECT * FROM ftpsserver WHERE lastModified >= :timestamp")
    suspend fun getServersModifiedSince(timestamp: Long): List<FtpsServer>
}