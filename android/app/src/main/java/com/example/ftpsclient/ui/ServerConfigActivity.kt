package com.example.ftpsclient.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ftpsclient.R
import com.example.ftpsclient.data.FtpsServer
import com.example.ftpsclient.service.FtpsService
import com.example.ftpsclient.viewmodel.ServerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerConfigActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SERVER_ID = "server_id"
    }

    private lateinit var viewModel: ServerViewModel
    private lateinit var ftpsService: FtpsService
    private var serverId: Int = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_config)

        viewModel = ViewModelProvider(this).get(ServerViewModel::class.java)
        ftpsService = FtpsService()

        serverId = intent.getIntExtra(EXTRA_SERVER_ID, 0)
        isEditMode = serverId != 0

        if (isEditMode) {
            title = getString(R.string.edit_server)
            loadServerData()
        }

        setupUI()
    }

    private fun loadServerData() {
        viewModel.getServerById(serverId).observe(this) { server ->
            server?.let { fillServerData(it) }
        }
    }

    private fun fillServerData(server: FtpsServer) {
        findViewById<EditText>(R.id.serverName).setText(server.name)
        findViewById<EditText>(R.id.hostname).setText(server.hostname)
        findViewById<EditText>(R.id.port).setText(server.port.toString())
        findViewById<EditText>(R.id.username).setText(server.username)
        findViewById<EditText>(R.id.password).setText(server.password)
        findViewById<EditText>(R.id.directory).setText(server.directory)
    }

    private fun setupUI() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.text = getString(if (isEditMode) R.string.update_server else R.string.save_server)

        findViewById<Button>(R.id.testButton).setOnClickListener { testConnection() }
        saveButton.setOnClickListener { saveServer() }
    }

    private fun getServerFromInput(): FtpsServer {
        return FtpsServer(
            id = serverId,
            name = findViewById<EditText>(R.id.serverName).text.toString(),
            hostname = findViewById<EditText>(R.id.hostname).text.toString(),
            port = findViewById<EditText>(R.id.port).text.toString().toIntOrNull() ?: 21,
            username = findViewById<EditText>(R.id.username).text.toString(),
            password = findViewById<EditText>(R.id.password).text.toString(),
            directory = findViewById<EditText>(R.id.directory).text.toString(),
        )
    }

    private fun saveServer() {
        if (validateInput()) {
            val server = getServerFromInput()
            if (isEditMode) {
                viewModel.updateServer(server)
                Toast.makeText(this, R.string.server_updated, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addServer(server)
                Toast.makeText(this, R.string.server_saved, Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
    
    private fun validateInput(): Boolean {
        val name = findViewById<EditText>(R.id.serverName).text.toString()
        val hostname = findViewById<EditText>(R.id.hostname).text.toString()
        val username = findViewById<EditText>(R.id.username).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        
        if (name.isBlank() || hostname.isBlank() || username.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun testConnection() {
        if (!validateInput()) return
        
        val server = getServerFromInput()
        val progressDialog = showProgressDialog(getString(R.string.testing_connection))
        println("going to test connection")
        lifecycleScope.launch(Dispatchers.IO) {
            println("ok, now doing it")
            val success = ftpsService.testConnection(server)
            println("got success: $success")
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                showTestResult(success)
            }
        }
    }
    
    private fun showProgressDialog(message: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .create()
            .apply { show() }
    }
    
    private fun showTestResult(success: Boolean) {
        val message = if (success) getString(R.string.connection_success) 
                     else getString(R.string.connection_failed)
        val icon = if (success) R.drawable.ic_success else R.drawable.ic_error
        
        AlertDialog.Builder(this)
            .setTitle(R.string.connection_test)
            .setMessage(message)
            .setIcon(icon)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
