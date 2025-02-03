package com.example.ftpsclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.ftpsclient.data.AppDatabase
import com.example.ftpsclient.data.FtpsServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "ftps-database"
    ).build()

    private val serverDao = database.ftpsServerDao()

    fun addServer(server: FtpsServer) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.insertServer(server)
        }
    }

    fun updateServer(server: FtpsServer) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.updateServer(server)
        }
    }

    fun deleteServer(server: FtpsServer) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.deleteServer(server)
        }
    }

    fun getServers(): LiveData<List<FtpsServer>> = liveData(Dispatchers.IO) {
        emit(serverDao.getAllServers())
    }

    fun getServerById(id: Int): LiveData<FtpsServer?> = liveData(Dispatchers.IO) {
        emit(serverDao.getServerById(id))
    }
}