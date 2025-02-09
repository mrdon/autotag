package com.example.ftpsclient.ui

import android.app.AlertDialog
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpsclient.R
import com.example.ftpsclient.data.FtpsServer
import com.example.ftpsclient.service.FtpsService
import com.example.ftpsclient.viewmodel.ServerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ServerViewModel
    private lateinit var ftpsService: FtpsService
    private lateinit var serverAdapter: ServerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(ServerViewModel::class.java)
        ftpsService = FtpsService()

        setupRecyclerView()
        setupAddButton()

        if (intent?.action == Intent.ACTION_SEND) {
            handleSharedFile()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.serverList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.getServers().observe(this) { servers ->
            serverAdapter = ServerAdapter(
                servers,
                onServerClick = { server ->
                    if (intent?.action == Intent.ACTION_SEND) {
                        uploadToServer(server, intent.getParcelableExtra(Intent.EXTRA_STREAM)!!)
                    }
                },
                onServerLongClick = { view, server ->
                    showServerOptionsMenu(view, server)
                }
            )
            recyclerView.adapter = serverAdapter
        }
    }

    private fun showServerOptionsMenu(view: View, server: FtpsServer) {
        PopupMenu(this, view).apply {
            menu.add(0, 0, 0, R.string.test_connection)
            menu.add(0, 1, 0, R.string.edit_server)
            menu.add(0, 2, 0, R.string.delete_server)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> testServerConnection(server)
                    1 -> editServer(server)
                    2 -> confirmDeleteServer(server)
                }
                true
            }
            show()
        }
    }

    private fun testServerConnection(server: FtpsServer) {
        val progressDialog = showProgressDialog(getString(R.string.testing_connection))

        lifecycleScope.launch(Dispatchers.IO) {
            val success = ftpsService.testConnection(server)

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                showTestResult(success)
            }
        }
    }

    private fun editServer(server: FtpsServer) {
        val intent = Intent(this, ServerConfigActivity::class.java).apply {
            putExtra(ServerConfigActivity.EXTRA_SERVER_ID, server.id)
        }
        startActivity(intent)
    }

    private fun confirmDeleteServer(server: FtpsServer) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_server_title)
            .setMessage(getString(R.string.delete_server_message, server.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteServer(server)
                Toast.makeText(this, R.string.server_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showProgressDialog(message: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .create()
            .apply { show() }
    }

    private fun showTestResult(success: Boolean) {
        val message = if (success) R.string.connection_success else R.string.connection_failed
        val icon = if (success) R.drawable.ic_success else R.drawable.ic_error

        AlertDialog.Builder(this)
            .setTitle(R.string.connection_test)
            .setMessage(message)
            .setIcon(icon)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    private fun setupAddButton() {
        findViewById<Button>(R.id.addServerButton).setOnClickListener {
            startActivity(Intent(this, ServerConfigActivity::class.java))
        }
    }
    
    private fun handleSharedFile() {
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        uri?.let { fileUri ->
            viewModel.getServers().observe(this) { servers ->
                showServerSelectionDialog(servers, fileUri)
            }
        }
    }
    
    private fun showServerSelectionDialog(servers: List<FtpsServer>, fileUri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(R.string.select_server)
            .setItems(servers.map { it.name }.toTypedArray()) { _, which ->
                uploadToServer(servers[which], fileUri)
            }
            .show()
    }
    
    private fun uploadToServer(server: FtpsServer, fileUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {

            val filePath = getRealPathFromURI(fileUri)
            val fileName = File(filePath).name
            println("Sending $fileName")
            
            if (ftpsService.connect(server)) {
                println("Connected")
                val success = try {
                    val inputPFD : ParcelFileDescriptor? = try {
                        contentResolver.openFileDescriptor(fileUri, "r")
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Log.e("MainActivity", "File not found")
                        null
                    }

                    if (inputPFD != null) {
                        val fd = inputPFD.fileDescriptor
                        ftpsService.uploadFile(fileName, fd)
                        inputPFD.close()
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    println("Unable to upload file to server: $e")
                    false
                }

//                val success = ftpsService.uploadFile(filePath, fileName)
                ftpsService.disconnect()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        if (success) R.string.upload_success else R.string.upload_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                return it.getString(idx)
            }
        }
        return uri.path ?: throw IllegalArgumentException("Invalid URI")
    }
}
