package com.example.plannotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.R
import com.example.plannotes.data.Account
import com.example.plannotes.data.AccountSummary
import java.util.Locale

class AccountAdapter(
    private var accounts: List<Account>,
    private var summaries: Map<String, AccountSummary>,
    private val onItemClick: (Account) -> Unit,
    private val onItemLongClick: (Account) -> Unit,
    private val onProfitClick: (Account) -> Unit,
    private val onAbandonClick: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_account_name)
        val tvStage: TextView = view.findViewById(R.id.tv_stage)
        val tvPrincipal: TextView = view.findViewById(R.id.tv_principal)
        val tvProfit: TextView = view.findViewById(R.id.tv_profit)
        val tvRecordCount: TextView = view.findViewById(R.id.tv_record_count)
        val btnProfit: Button = view.findViewById(R.id.btn_profit)
        val btnAbandon: Button = view.findViewById(R.id.btn_abandon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        val summary = summaries[account.id]
        
        holder.tvName.text = account.name
        holder.tvStage.text = "阶段: ${account.currentStage}"
        
        val principal = summary?.principal ?: 0.0
        val totalProfit = summary?.totalProfit ?: 0.0
        holder.tvPrincipal.text = "本金: ${formatCurrency(principal)}"
        holder.tvProfit.text = "盈利: ${formatCurrency(totalProfit)}"
        
        val count = summary?.recordCount ?: 0
        holder.tvRecordCount.text = "数量: ${account.quantity} | $count 条记录"
        
        if (account.currentStage == 0) {
            holder.btnProfit.isEnabled = false
            holder.btnAbandon.isEnabled = false
            holder.btnProfit.alpha = 0.5f
            holder.btnAbandon.alpha = 0.5f
        } else {
            holder.btnProfit.isEnabled = true
            holder.btnAbandon.isEnabled = true
            holder.btnProfit.alpha = 1.0f
            holder.btnAbandon.alpha = 1.0f
            holder.btnProfit.setOnClickListener { onProfitClick(account) }
            holder.btnAbandon.setOnClickListener { onAbandonClick(account) }
        }
        
        holder.itemView.setOnClickListener { onItemClick(account) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(account)
            true
        }
    }
    
    override fun getItemCount() = accounts.size
    
    fun updateAccounts(newAccounts: List<Account>, newSummaries: Map<String, AccountSummary>) {
        accounts = newAccounts
        summaries = newSummaries
        notifyDataSetChanged()
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}
