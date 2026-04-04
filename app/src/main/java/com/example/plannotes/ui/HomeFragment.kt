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
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountAdapter
    private val accounts get() = (activity as MainActivity).dataManager.getAccounts()
    
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
        val fabAdd: FloatingActionButton = view.findViewById(R.id.fab_add_account)
        
        adapter = AccountAdapter(
            accounts = accounts,
            onItemClick = { account ->
                (activity as MainActivity).showAccountDetailFragment(account.id)
            },
            onItemLongClick = { account ->
                showAccountOptions(account)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        fabAdd.setOnClickListener {
            addNewAccount()
        }
    }
    
    override fun onResume() {
        super.onResume()
        adapter.updateAccounts(accounts)
    }
    
    private fun showAccountOptions(account: Account) {
        val options = arrayOf(
            getString(R.string.rename),
            getString(R.string.delete)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(account.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameAccount(account)
                    1 -> deleteAccount(account)
                }
            }
            .show()
    }
    
    private fun renameAccount(account: Account) {
        (activity as MainActivity).showRenameDialog(account.id, account.name) { newName ->
            account.name = newName
            (activity as MainActivity).dataManager.updateAccount(account)
            adapter.updateAccounts(accounts)
        }
    }
    
    private fun deleteAccount(account: Account) {
        (activity as MainActivity).showDeleteConfirmDialog {
            (activity as MainActivity).dataManager.deleteAccount(account.id)
            adapter.updateAccounts(accounts)
            Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addNewAccount() {
        (activity as MainActivity).showRenameDialog("", "") { name ->
            if (name.isNotEmpty()) {
                val account = Account(name = name)
                (activity as MainActivity).dataManager.addAccount(account)
                adapter.updateAccounts(accounts)
            }
        }
    }
}
