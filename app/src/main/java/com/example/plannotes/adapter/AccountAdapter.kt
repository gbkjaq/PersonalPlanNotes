package com.example.plannotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.R
import com.example.plannotes.data.Account

class AccountAdapter(
    private var accounts: List<Account>,
    private val recordCounts: Map<String, Int>,
    private val onItemClick: (Account) -> Unit,
    private val onItemLongClick: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_account_name)
        val tvRecordCount: TextView = view.findViewById(R.id.tv_record_count)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.tvName.text = account.name
        val count = recordCounts[account.id] ?: 0
        holder.tvRecordCount.text = "$count 条记录"
        holder.itemView.setOnClickListener { onItemClick(account) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(account)
            true
        }
    }
    
    override fun getItemCount() = accounts.size
    
    fun updateAccounts(newAccounts: List<Account>, newRecordCounts: Map<String, Int>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
}
