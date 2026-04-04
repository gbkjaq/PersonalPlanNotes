package com.example.plannotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.plannotes.data.DataManager
import com.example.plannotes.ui.AccountDetailFragment
import com.example.plannotes.ui.HomeFragment
import com.example.plannotes.ui.SettingsFragment
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    
    lateinit var dataManager: DataManager
    lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        dataManager = DataManager(this)
        
        if (savedInstanceState == null) {
            showHomeFragment()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    fun showHomeFragment() {
        replaceFragment(HomeFragment())
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = getString(R.string.app_name)
    }
    
    fun showAccountDetailFragment(accountId: String) {
        replaceFragment(AccountDetailFragment.newInstance(accountId))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    fun showSettingsFragment() {
        replaceFragment(SettingsFragment())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.settings)
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    fun showRenameDialog(accountId: String, currentName: String, callback: (String) -> Unit) {
        val editText = EditText(this).apply {
            setText(currentName)
        }
        
        val title = if (currentName.isEmpty()) R.string.add_account else R.string.rename
        
        AlertDialog.Builder(this)
            .setTitle(title)
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
