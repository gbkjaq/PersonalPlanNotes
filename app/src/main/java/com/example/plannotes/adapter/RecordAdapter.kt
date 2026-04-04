package com.example.plannotes.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.R
import com.example.plannotes.data.RecordDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter(
    private var records: List<RecordDisplay>,
    private val onItemClick: (RecordDisplay) -> Unit,
    private val onItemLongClick: (RecordDisplay) -> Unit,
    private val onProfitClick: (RecordDisplay) -> Unit,
    private val onAbandonClick: (RecordDisplay) -> Unit
) : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tv_index)
        val tvStage: TextView = view.findViewById(R.id.tv_stage)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        val tvPrincipal: TextView = view.findViewById(R.id.tv_principal)
        val tvProfit: TextView = view.findViewById(R.id.tv_profit)
        val tvTotalProfit: TextView = view.findViewById(R.id.tv_total_profit)
        val tvRemark: TextView = view.findViewById(R.id.tv_remark)
        val btnProfit: Button = view.findViewById(R.id.btn_profit)
        val btnAbandon: Button = view.findViewById(R.id.btn_abandon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recordDisplay = records[position]
        val record = recordDisplay.record
        
        holder.tvIndex.text = recordDisplay.index.toString()
        holder.tvStage.text = record.stage.toString()
        holder.tvDate.text = formatDate(record.createTime)
        holder.tvAmount.text = formatCurrency(record.amount)
        holder.tvPrincipal.text = formatCurrency(recordDisplay.principal)
        holder.tvProfit.text = formatCurrency(recordDisplay.profit)
        holder.tvTotalProfit.text = formatCurrency(recordDisplay.totalProfit)
        
        if (record.remark.isNotEmpty()) {
            holder.tvRemark.visibility = View.VISIBLE
            holder.tvRemark.text = record.remark
        } else {
            holder.tvRemark.visibility = View.GONE
        }
        
        when (record.status) {
            1 -> {
                holder.btnProfit.text = "已盈利"
                holder.btnProfit.setBackgroundColor(Color.GREEN)
                holder.btnAbandon.visibility = View.GONE
            }
            2 -> {
                holder.btnAbandon.text = "已放弃"
                holder.btnAbandon.setBackgroundColor(Color.RED)
                holder.btnProfit.visibility = View.GONE
            }
            else -> {
                holder.btnProfit.text = holder.itemView.context.getString(R.string.profit_action)
                holder.btnProfit.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.btnProfit.visibility = View.VISIBLE
                holder.btnAbandon.text = holder.itemView.context.getString(R.string.abandon)
                holder.btnAbandon.setBackgroundColor(Color.parseColor("#F44336"))
                holder.btnAbandon.visibility = View.VISIBLE
            }
        }
        
        holder.btnProfit.setOnClickListener { onProfitClick(recordDisplay) }
        holder.btnAbandon.setOnClickListener { onAbandonClick(recordDisplay) }
        
        holder.itemView.setOnClickListener { onItemClick(recordDisplay) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(recordDisplay)
            true
        }
    }
    
    override fun getItemCount() = records.size
    
    fun updateRecords(newRecords: List<RecordDisplay>) {
        records = newRecords
        notifyDataSetChanged()
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}
