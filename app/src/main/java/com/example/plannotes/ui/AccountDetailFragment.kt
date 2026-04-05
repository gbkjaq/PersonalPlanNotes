package com.example.plannotes.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.data.RecordDisplay
import com.example.plannotes.adapter.RecordAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    
    private var accountId: String = ""
    private var recyclerView: RecyclerView? = null
    private var adapter: RecordAdapter? = null
    private var tvStage: TextView? = null
    private var tvTotalPrincipal: TextView? = null
    private var tvTotalProfit: TextView? = null
    private var tvTotalRevenue: TextView? = null
    private var btnChangeStage: Button? = null
    private var fabAdd: FloatingActionButton? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountId = arguments?.getString(ARG_ACCOUNT_ID) ?: ""
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_detail, container, false)
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_records)
        tvStage = view.findViewById(R.id.tv_current_stage)
        tvTotalPrincipal = view.findViewById(R.id.tv_total_principal)
        tvTotalProfit = view.findViewById(R.id.tv_total_profit)
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue)
        btnChangeStage = view.findViewById(R.id.btn_change_stage)
        fabAdd = view.findViewById(R.id.fab_add_record)
        
        adapter = RecordAdapter(
            records = emptyList(),
            onItemClick = { recordDisplay ->
                showEditRecordDialog(recordDisplay)
            },
            onItemLongClick = { recordDisplay ->
                showDeleteRecordDialog(recordDisplay)
            }
        )
        
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter
        
        setupDraggableFab(view)
        
        btnChangeStage?.setOnClickListener {
            val activity = activity as? MainActivity ?: return@setOnClickListener
            val account = activity.dataManager.getAccounts().find { it.id == accountId } ?: return@setOnClickListener
            activity.showChangeStageDialog(account.currentStage) { newStage ->
                account.currentStage = newStage
                activity.dataManager.updateAccount(account)
                loadData()
            }
        }
        
        loadData()
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDraggableFab(rootView: View) {
        var dX = 0f
        var dY = 0f
        var totalMoved = 0f
        var clickInitiated = false
        
        fabAdd?.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    totalMoved = 0f
                    clickInitiated = true
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    
                    totalMoved += kotlin.math.abs(event.rawX - (view.x - dX)) + kotlin.math.abs(event.rawY - (view.y - dY))
                    
                    val parent = view.parent as? View ?: return@setOnTouchListener false
                    val maxX = parent.width - view.width.toFloat()
                    val maxY = parent.height - view.height.toFloat()
                    
                    view.x = newX.coerceIn(0f, maxX)
                    view.y = newY.coerceIn(0f, maxY)
                    
                    if (totalMoved > 10f) {
                        clickInitiated = false
                    }
                    totalMoved > 10f
                }
                MotionEvent.ACTION_UP -> {
                    if (clickInitiated && totalMoved <= 10f) {
                        fabAdd?.performClick()
                    }
                    clickInitiated
                }
                else -> false
            }
        }
        
        fabAdd?.setOnClickListener {
            showAddRecordDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
    }
    
    private fun loadData() {
        val activity = activity as? MainActivity ?: return
        val account = activity.dataManager.getAccounts().find { it.id == accountId } ?: return
        val config = activity.dataManager.getConfig()
        val records = activity.dataManager.getRecordsWithDisplay(accountId, config, account.currentStage)
        
        adapter?.updateRecords(records)
        
        tvStage?.text = getString(R.string.current_stage_format, account.currentStage)
        
        var totalPrincipal = 0.0
        var totalProfit = 0.0
        var totalRevenue = 0.0
        
        if (records.isNotEmpty()) {
            val lastRecord = records.last()
            totalPrincipal = lastRecord.principal
            totalProfit = lastRecord.profit
            totalRevenue = lastRecord.totalProfit
        }
        
        tvTotalPrincipal?.text = formatCurrency(totalPrincipal)
        tvTotalProfit?.text = formatCurrency(totalProfit)
        tvTotalRevenue?.text = formatCurrency(totalRevenue)
    }
    
    private fun showAddRecordDialog() {
        val activity = activity as? MainActivity ?: return
        if (accountId.isEmpty()) {
            Toast.makeText(requireContext(), "账户无效", Toast.LENGTH_SHORT).show()
            return
        }
        val context = requireContext()
        
        val currentCount = activity.dataManager.getRecords(accountId).size
        val nextStage = currentCount + 1
        
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        
        val tvStage = android.widget.TextView(context).apply {
            text = "第 $nextStage 阶段"
            textSize = 16f
        }
        
        val etAmount = EditText(context).apply {
            hint = getString(R.string.amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        
        val etRemark = EditText(context).apply {
            hint = getString(R.string.remark)
        }
        
        layout.addView(tvStage)
        layout.addView(etAmount)
        layout.addView(etRemark)
        
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(R.string.add_record)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val amountStr = etAmount.text.toString()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val remarkText = etRemark.text.toString()
                    val record = com.example.plannotes.data.Record(
                        amount = amount,
                        stage = nextStage,
                        remark = remarkText
                    )
                    activity.dataManager.addRecord(accountId, record)
                    
                    val account = activity.dataManager.getAccounts().find { it.id == accountId }
                    account?.let {
                        it.currentStage = nextStage
                        activity.dataManager.updateAccount(it)
                    }
                    
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
                val activity = activity as? MainActivity ?: return@setPositiveButton
                val amountStr = etAmount.text.toString()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val remarkText = etRemark.text.toString()
                    val record = com.example.plannotes.data.Record(
                        id = recordDisplay.record.id,
                        amount = amount,
                        stage = recordDisplay.record.stage,
                        remark = remarkText,
                        createTime = recordDisplay.record.createTime,
                        status = recordDisplay.record.status
                    )
                    activity.dataManager.updateRecord(accountId, record)
                    loadData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showDeleteRecordDialog(recordDisplay: RecordDisplay) {
        val activity = activity as? MainActivity ?: return
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                activity.dataManager.deleteRecord(accountId, recordDisplay.record.id)
                loadData()
                Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}
