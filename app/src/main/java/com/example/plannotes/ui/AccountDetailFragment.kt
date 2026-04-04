package com.example.plannotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.data.Config
import com.example.plannotes.data.Record
import com.example.plannotes.data.RecordDisplay
import com.example.plannotes.adapter.RecordAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountDetailFragment : Fragment() {
    
    companion object {
        private const val ARG_ACCOUNT_ID = "account_id"
        
        fun newInstance(accountId: String): AccountDetailFragment {
            return AccountDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCOUNT_ID, accountId)
                }
            }
        }
    }
    
    private lateinit var accountId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordAdapter
    private lateinit var tvTotalPrincipal: TextView
    private lateinit var tvTotalProfit: TextView
    private lateinit var tvTotalRevenue: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountId = arguments?.getString(ARG_ACCOUNT_ID) ?: return
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_records)
        tvTotalPrincipal = view.findViewById(R.id.tv_total_principal)
        tvTotalProfit = view.findViewById(R.id.tv_total_profit)
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue)
        val fabAdd: FloatingActionButton = view.findViewById(R.id.fab_add_record)
        
        adapter = RecordAdapter(
            records = emptyList(),
            onItemClick = { recordDisplay ->
                showEditRecordDialog(recordDisplay)
            },
            onItemLongClick = { recordDisplay ->
                showDeleteRecordDialog(recordDisplay)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        fabAdd.setOnClickListener {
            showAddRecordDialog()
        }
        
        loadData()
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
        (activity as? MainActivity)?.title = (activity as MainActivity).dataManager.getAccounts()
            .find { it.id == accountId }?.name
    }
    
    private fun loadData() {
        val accountWithRecords = (activity as MainActivity).dataManager.getAccountWithRecords(accountId)
        val records = accountWithRecords.records
        
        adapter.updateRecords(records)
        
        val lastRecord = records.lastOrNull()
        tvTotalPrincipal.text = formatCurrency(lastRecord?.principal ?: 0.0)
        tvTotalProfit.text = formatCurrency(lastRecord?.profit ?: 0.0)
        tvTotalRevenue.text = formatCurrency(lastRecord?.totalProfit ?: 0.0)
        
        (activity as? MainActivity)?.title = accountWithRecords.account.name
    }
    
    private fun showAddRecordDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        
        val etAmount = EditText(context).apply {
            hint = getString(R.string.amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        
        val etRemark = EditText(context).apply {
            hint = getString(R.string.remark)
        }
        
        layout.addView(etAmount)
        layout.addView(etRemark)
        
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(R.string.add_record)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val amountStr = etAmount.text.toString()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val remark = etRemark.text.toString()
                    val record = Record(amount = amount, remark = remark)
                    (activity as MainActivity).dataManager.addRecord(accountId, record)
                    loadData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showEditRecordDialog(recordDisplay: RecordDisplay) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        
        val etAmount = EditText(context).apply {
            hint = getString(R.string.amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(recordDisplay.record.amount.toString())
        }
        
        val etRemark = EditText(context).apply {
            hint = getString(R.string.remark)
            setText(recordDisplay.record.remark)
        }
        
        layout.addView(etAmount)
        layout.addView(etRemark)
        
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(R.string.edit_record)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val amountStr = etAmount.text.toString()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val remark = etRemark.text.toString()
                    val record = Record(
                        id = recordDisplay.record.id,
                        amount = amount,
                        remark = remark,
                        createTime = recordDisplay.record.createTime
                    )
                    (activity as MainActivity).dataManager.updateRecord(accountId, record)
                    loadData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showDeleteRecordDialog(recordDisplay: RecordDisplay) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                (activity as MainActivity).dataManager.deleteRecord(accountId, recordDisplay.record.id)
                loadData()
                Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
