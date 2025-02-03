package com.example.ftpsclient.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpsclient.R
import com.example.ftpsclient.data.FtpsServer

class ServerAdapter(
    private val servers: List<FtpsServer>,
    private val onServerClick: (FtpsServer) -> Unit,
    private val onServerLongClick: (View, FtpsServer) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.serverName)
        val hostText: TextView = view.findViewById(R.id.serverHost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        holder.nameText.text = server.name
        holder.hostText.text = "${server.hostname}:${server.port}"

        holder.itemView.setOnClickListener { onServerClick(server) }
        holder.itemView.setOnLongClickListener { view ->
            onServerLongClick(view, server)
            true
        }
    }

    override fun getItemCount() = servers.size
}