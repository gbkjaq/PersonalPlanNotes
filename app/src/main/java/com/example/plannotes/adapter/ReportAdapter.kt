package com.example.plannotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.R
import com.example.plannotes.data.ReportItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
    
    private var items = listOf<ReportItem>()
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAccount: TextView = view.findViewById(R.id.tv_account_name)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvRemark: TextView = view.findViewById(R.id.tv_remark)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvAccount.text = item.accountName
        holder.tvAmount.text = formatCurrency(item.record.amount)
        holder.tvDate.text = formatDate(item.record.createTime)
        
        if (item.record.remark.isNotEmpty()) {
            holder.tvRemark.visibility = View.VISIBLE
            holder.tvRemark.text = item.record.remark
        } else {
            holder.tvRemark.visibility = View.GONE
        }
    }
    
    override fun getItemCount() = items.size
    
    fun updateRecords(newItems: List<ReportItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}
