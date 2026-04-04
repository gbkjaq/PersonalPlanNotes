package com.example.plannotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.plannotes.data.DataManager
import com.example.plannotes.ui.HomeFragment
import com.example.plannotes.ui.AccountDetailFragment
import com.example.plannotes.ui.SettingsFragment
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    
    lateinit var dataManager: DataManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        dataManager = DataManager(this)
        
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    fun showAccountDetailFragment(accountId: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AccountDetailFragment.newInstance(accountId))
            .addToBackStack(null)
            .commit()
    }
    
    fun showRenameDialog(currentName: String, callback: (String) -> Unit) {
        val editText = EditText(this).apply {
            setText(currentName)
        }
        
        val titleRes = if (currentName.isEmpty()) R.string.add_account else R.string.rename
        
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    callback(newName)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    fun showDeleteConfirmDialog(callback: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ -> callback() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
