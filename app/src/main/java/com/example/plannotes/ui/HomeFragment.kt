package com.example.plannotes.ui

import android.os.Bundle
import android.view.LayoutInflater
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

class HomeFragment : Fragment() {
    
    private var recyclerView: RecyclerView? = null
    private var adapter: AccountAdapter? = null
    
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
        val fabAdd: FloatingActionButton? = view.findViewById(R.id.fab_add_account)
        
        val activity = activity as? MainActivity ?: return
        val accounts = activity.dataManager.getAccounts()
        val recordCounts = accounts.associate { it.id to activity.dataManager.getRecords(it.id).size }
        
        adapter = AccountAdapter(
            accounts = accounts,
            recordCounts = recordCounts,
            onItemClick = { account ->
                (activity as MainActivity).showAccountDetailFragment(account.id)
            },
            onItemLongClick = { account ->
                showAccountOptions(account)
            }
        )
        
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter
        
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
        val recordCounts = accounts.associate { it.id to activity.dataManager.getRecords(it.id).size }
        adapter?.updateAccounts(accounts, recordCounts)
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
        activity.showRenameDialog(account.name, account.quantity) { newName, newQuantity ->
            account.name = newName
            account.quantity = newQuantity
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
        activity.showRenameDialog("", 1) { name, quantity ->
            if (name.isNotEmpty()) {
                val account = Account(name = name, quantity = quantity)
                activity.dataManager.addAccount(account)
                refreshData()
            }
        }
    }
}
