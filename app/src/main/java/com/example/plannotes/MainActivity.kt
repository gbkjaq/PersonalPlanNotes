package com.example.plannotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.plannotes.data.Account
import com.example.plannotes.data.Config
import com.example.plannotes.data.Record
import com.example.plannotes.ui.HomeFragment
import com.example.plannotes.ui.AccountDetailFragment
import com.example.plannotes.ui.SettingsFragment
import com.example.plannotes.ui.ReportFragment
import com.example.plannotes.data.DataManager
import com.google.gson.Gson
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    
    lateinit var dataManager: DataManager
    private val gson = Gson()
    
    private var exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportData(it) }
    }
    
    private var importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importData(it) }
    }
    
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
            R.id.action_report -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ReportFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
                true
            }
            R.id.action_export -> {
                exportLauncher.launch("plan_notes_backup.json")
                true
            }
            R.id.action_import -> {
                showImportConfirmDialog()
                true
            }
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
    
    private fun showImportConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.import_data)
            .setMessage(R.string.import_confirm)
            .setPositiveButton(R.string.confirm) { _, _ ->
                importLauncher.launch(arrayOf("application/json"))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun exportData(uri: android.net.Uri) {
        try {
            val config = dataManager.getConfig()
            val accounts = dataManager.getAccounts()
            val recordsMap = mutableMapOf<String, List<Record>>()
            
            for (account in accounts) {
                recordsMap[account.id] = dataManager.getRecords(account.id)
            }
            
            val backup = JSONObject().apply {
                put("config", JSONObject().apply {
                    put("quantity", config.quantity)
                    put("coefficient", config.coefficient)
                })
                put("accounts", gson.toJson(accounts))
                put("records", gson.toJson(recordsMap))
                put("version", 1)
                put("exportTime", System.currentTimeMillis())
            }
            
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(backup.toString().toByteArray())
            }
            
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun importData(uri: android.net.Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: throw Exception("Cannot read file")
            
            val backup = JSONObject(json)
            
            val configObj = backup.getJSONObject("config")
            val config = Config(
                quantity = configObj.getInt("quantity"),
                coefficient = configObj.getInt("coefficient")
            )
            dataManager.saveConfig(config)
            
            val accountsJson = backup.getString("accounts")
            val accounts = gson.fromJson(accountsJson, Array<Account>::class.java).toList()
            dataManager.saveAccounts(accounts)
            
            val recordsJson = backup.getString("records")
            val recordsMap = gson.fromJson(recordsJson, Map::class.java) as Map<String, List<Record>>
            
            for ((accountId, records) in recordsMap) {
                dataManager.saveRecords(accountId, records)
            }
            
            Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show()
            
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show()
        }
    }
    
    fun showAccountDetailFragment(accountId: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AccountDetailFragment.newInstance(accountId))
            .addToBackStack(null)
            .commit()
    }
    
    fun showRenameDialog(currentName: String, currentQuantity: Int = 1, callback: (String, Int) -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        
        val editText = EditText(this).apply {
            hint = getString(R.string.account_name)
            setText(currentName)
        }
        
        val etQuantity = EditText(this).apply {
            hint = getString(R.string.quantity)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentQuantity.toString())
        }
        
        layout.addView(editText)
        layout.addView(etQuantity)
        
        val titleRes = if (currentName.isEmpty()) R.string.add_account else R.string.rename
        
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editText.text.toString().trim()
                val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
                if (newName.isNotEmpty()) {
                    callback(newName, quantity)
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
    
    fun showChangeStageDialog(currentStage: Int, callback: (Int) -> Unit) {
        val editText = EditText(this).apply {
            hint = getString(R.string.stage)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentStage.toString())
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.change_stage)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val stage = editText.text.toString().toIntOrNull() ?: 1
                if (stage > 0) {
                    callback(stage)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    fun showWholeAccountProfitDialog(account: Account) {
        AlertDialog.Builder(this)
            .setTitle(R.string.profit_action)
            .setMessage("将账本「${account.name}」所有记录标记为盈利？")
            .setPositiveButton(R.string.confirm) { _, _ ->
                val records = dataManager.getRecords(account.id)
                for (record in records) {
                    record.status = com.example.plannotes.data.Record.STATUS_PROFIT
                    dataManager.updateRecord(account.id, record)
                }
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    fun showWholeAccountAbandonDialog(account: Account) {
        AlertDialog.Builder(this)
            .setTitle(R.string.abandon)
            .setMessage("将账本「${account.name}」所有记录标记为放弃？")
            .setPositiveButton(R.string.confirm) { _, _ ->
                val records = dataManager.getRecords(account.id)
                for (record in records) {
                    record.status = com.example.plannotes.data.Record.STATUS_ABANDON
                    dataManager.updateRecord(account.id, record)
                }
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
