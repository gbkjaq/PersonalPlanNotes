package com.example.plannotes.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.data.Account
import com.example.plannotes.adapter.AccountAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class HomeFragment : Fragment() {
    
    private var recyclerView: RecyclerView? = null
    private var adapter: AccountAdapter? = null
    private var fabAdd: FloatingActionButton? = null
    private var tvTotalPrincipal: android.widget.TextView? = null
    private var tvTotalProfit: android.widget.TextView? = null
    private var tvAccountCount: android.widget.TextView? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_accounts)
        fabAdd = view.findViewById(R.id.fab_add_account)
        tvTotalPrincipal = view.findViewById(R.id.tv_total_principal)
        tvTotalProfit = view.findViewById(R.id.tv_total_profit)
        tvAccountCount = view.findViewById(R.id.tv_account_count)
        
        val activity = activity as? MainActivity ?: return
        val accounts = activity.dataManager.getAccounts()
        val summaries = activity.dataManager.getAccountSummaries()
        val defaultCoefficient = activity.dataManager.getConfig().coefficient
        
        adapter = AccountAdapter(
            accounts = accounts,
            summaries = summaries,
            defaultCoefficient = defaultCoefficient,
            onItemClick = { account ->
                (activity as MainActivity).showAccountDetailFragment(account.id)
            },
            onItemLongClick = { account ->
                showAccountOptions(account)
            },
            onProfitClick = { account ->
                activity.showWholeAccountProfitDialog(account)
                refreshData()
            },
            onAbandonClick = { account ->
                activity.showWholeAccountAbandonDialog(account)
                refreshData()
            }
        )
        
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter
        
        refreshData()
        setupDraggableFab(view)
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
            addNewAccount()
        }
    }
    
    override fun onResume() {
        super.onResume()
        refreshData()
    }
    
    private fun refreshData() {
        val activity = activity as? MainActivity ?: return
        val accounts = activity.dataManager.getAccounts()
        val summaries = activity.dataManager.getAccountSummaries()
        val defaultCoefficient = activity.dataManager.getConfig().coefficient
        adapter?.updateAccounts(accounts, summaries, defaultCoefficient)
        
        // 计算汇总统计
        var totalPrincipal = 0.0
        var totalProfit = 0.0
        for (account in accounts) {
            val summary = summaries[account.id]
            totalPrincipal += summary?.principal ?: 0.0
            totalProfit += summary?.totalProfit ?: 0.0
        }
        tvTotalPrincipal?.text = formatCurrency(totalPrincipal)
        tvTotalProfit?.text = formatCurrency(totalProfit)
        tvAccountCount?.text = accounts.size.toString()
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
    
    private fun showAccountOptions(account: Account) {
        val options = arrayOf(
            getString(R.string.rename),
            getString(R.string.delete)
        )
        
        activity?.let {
            androidx.appcompat.app.AlertDialog.Builder(it)
                .setTitle(account.name)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> renameAccount(account)
                        1 -> deleteAccount(account)
                    }
                }
                .show()
        }
    }
    
    private fun renameAccount(account: Account) {
        val activity = activity as? MainActivity ?: return
        activity.showRenameDialog(account.name, account.quantity, account.coefficient, account.remark) { newName, newQuantity, newCoefficient, newRemark ->
            account.name = newName
            account.quantity = newQuantity
            account.coefficient = newCoefficient
            account.remark = newRemark
            activity.dataManager.updateAccount(account)
            refreshData()
        }
    }
    
    private fun deleteAccount(account: Account) {
        val activity = activity as? MainActivity ?: return
        activity.showDeleteConfirmDialog {
            activity.dataManager.deleteAccount(account.id)
            refreshData()
            Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addNewAccount() {
        val activity = activity as? MainActivity ?: return
        activity.showRenameDialog("", 1, null, "") { name, quantity, coefficient, remark ->
            if (name.isNotEmpty()) {
                val account = Account(name = name, quantity = quantity, coefficient = coefficient, remark = remark)
                activity.dataManager.addAccount(account)
                refreshData()
            }
        }
    }
}
