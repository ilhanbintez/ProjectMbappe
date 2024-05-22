package com.ilhanbintez.projectmbappe

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilhanbintez.projectmbappe.databinding.RecylerRowBinding

class PlayerAdapter(val playerlist : ArrayList<Player>) : RecyclerView.Adapter<PlayerAdapter.PlayerHolder>() {
    class PlayerHolder(val binding: RecylerRowBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerHolder {
        val binding = RecylerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlayerHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = playerlist.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,PlayerDetails::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",playerlist[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return playerlist.size
    }

}