package com.example.plannotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.adapter.ReportAdapter
import com.example.plannotes.data.Record
import com.google.android.material.tabs.TabLayout
import java.util.Locale

class ReportFragment : Fragment() {
    
    private var tabLayout: TabLayout? = null
    private var recyclerView: RecyclerView? = null
    private var tvEmpty: TextView? = null
    private var tvTotalProfit: TextView? = null
    private var tvTotalAbandon: TextView? = null
    private var adapter: ReportAdapter? = null
    
    private var profitRecords = listOf<ReportItem>()
    private var abandonRecords = listOf<ReportItem>()
    
    companion object {
        fun newInstance(): ReportFragment {
            return ReportFragment()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tabLayout = view.findViewById(R.id.tab_layout)
        recyclerView = view.findViewById(R.id.recycler_report)
        tvEmpty = view.findViewById(R.id.tv_empty)
        tvTotalProfit = view.findViewById(R.id.tv_total_profit_amount)
        tvTotalAbandon = view.findViewById(R.id.tv_total_abandon_amount)
        
        tabLayout?.addTab(tabLayout?.newTab()?.setText(R.string.profit_record) ?: return)
        tabLayout?.addTab(tabLayout?.newTab()?.setText(R.string.abandon_record) ?: return)
        
        adapter = ReportAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter
        
        loadData()
        
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadData() {
        val activity = activity as? MainActivity ?: return
        val accounts = activity.dataManager.getAccounts()
        
        profitRecords = mutableListOf()
        abandonRecords = mutableListOf()
        
        var totalProfitAmount = 0.0
        var totalAbandonAmount = 0.0
        
        for (account in accounts) {
            val records = activity.dataManager.getRecords(account.id)
            for (record in records) {
                when (record.status) {
                    Record.STATUS_PROFIT -> {
                        totalProfitAmount += record.amount
                        (profitRecords as MutableList).add(ReportItem(account.name, record))
                    }
                    Record.STATUS_ABANDON -> {
                        totalAbandonAmount += record.amount
                        (abandonRecords as MutableList).add(ReportItem(account.name, record))
                    }
                }
            }
        }
        
        tvTotalProfit?.text = formatCurrency(totalProfitAmount)
        tvTotalAbandon?.text = formatCurrency(totalAbandonAmount)
        
        updateList()
    }
    
    private fun updateList() {
        val isProfitTab = tabLayout?.selectedTabPosition == 0
        val records = if (isProfitTab) profitRecords else abandonRecords
        
        adapter?.updateRecords(records)
        
        if (records.isEmpty()) {
            recyclerView?.visibility = View.GONE
            tvEmpty?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            tvEmpty?.visibility = View.GONE
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}

data class ReportItem(
    val accountName: String,
    val record: Record
)
