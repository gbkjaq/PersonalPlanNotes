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
import androidx.appcompat.widget.Toolbar
import com.example.plannotes.data.Account
import com.example.plannotes.data.Config
import com.example.plannotes.data.Record
import com.example.plannotes.data.ProfitLossRecord
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
    
    private var exportIncludeReport = true
    
    private var exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportToUri(it, exportIncludeReport) }
    }
    
    private var importIncludeReport = true
    
    private var importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importData(it, importIncludeReport) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
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
                showExportOptionsDialog()
                true
            }
            R.id.action_import -> {
                showImportOptionsDialog()
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
    
    private fun showExportOptionsDialog() {
        val options = arrayOf("导出全部（含报表）", "仅导出计划列表")
        AlertDialog.Builder(this)
            .setTitle("选择导出方式")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        exportIncludeReport = true
                        exportLauncher.launch("plan_notes_backup_full.json")
                    }
                    1 -> {
                        exportIncludeReport = false
                        exportLauncher.launch("plan_notes_backup.json")
                    }
                }
            }
            .show()
    }
    
    private fun exportToUri(uri: android.net.Uri, includeReport: Boolean) {
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
                if (includeReport) {
                    val profitLossRecords = dataManager.getProfitLossRecords()
                    put("profitLossRecords", gson.toJson(profitLossRecords))
                }
                put("version", 2)
                put("exportTime", System.currentTimeMillis())
            }
            
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(backup.toString().toByteArray())
            }
            
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showImportOptionsDialog() {
        val options = arrayOf("导入全部（含报表）", "仅导入计划列表")
        AlertDialog.Builder(this)
            .setTitle("选择导入方式")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        importIncludeReport = true
                        importLauncher.launch(arrayOf("application/json"))
                    }
                    1 -> {
                        importIncludeReport = false
                        importLauncher.launch(arrayOf("application/json"))
                    }
                }
            }
            .show()
    }
    
    private fun importData(uri: android.net.Uri, includeReport: Boolean) {
        try {
            val json = contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: throw Exception("Cannot read file")
            
            val backup = JSONObject(json)
            
            val configObj = backup.getJSONObject("config")
            val config = Config(
                quantity = configObj.getInt("quantity"),
                coefficient = configObj.getDouble("coefficient")
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
            
            if (includeReport && backup.has("profitLossRecords")) {
                val profitLossJson = backup.getString("profitLossRecords")
                val profitLossRecords = gson.fromJson(profitLossJson, Array<ProfitLossRecord>::class.java).toList()
                val existingRecords = dataManager.getProfitLossRecords().toMutableList()
                existingRecords.addAll(profitLossRecords)
                val prefs = getSharedPreferences("plan_notes", MODE_PRIVATE)
                prefs.edit().putString("profit_loss_records", gson.toJson(existingRecords)).apply()
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
    
    fun showRenameDialog(currentName: String, currentQuantity: Int = 1, currentCoefficient: Double? = null, currentRemark: String = "", callback: (String, Int, Double?, String) -> Unit) {
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
        
        val etCoefficient = EditText(this).apply {
            hint = "系数（留空使用系统配置）"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            currentCoefficient?.let { setText(it.toString()) }
        }
        
        val etRemark = EditText(this).apply {
            hint = "备注"
            setText(currentRemark)
        }
        
        layout.addView(editText)
        layout.addView(etQuantity)
        layout.addView(etCoefficient)
        layout.addView(etRemark)
        
        val titleRes = if (currentName.isEmpty()) R.string.add_account else R.string.rename
        
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editText.text.toString().trim()
                val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
                val coefficientStr = etCoefficient.text.toString()
                val coefficient = if (coefficientStr.isNotEmpty()) {
                    coefficientStr.toDoubleOrNull()
                } else {
                    null
                }
                val remark = etRemark.text.toString().trim()
                if (newName.isNotEmpty()) {
                    callback(newName, quantity, coefficient, remark)
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
            .setMessage("将账本「${account.name}」第${account.currentStage}阶段标记为盈利？计划将重置为0阶段。")
            .setPositiveButton(R.string.confirm) { _, _ ->
                val config = dataManager.getConfig()
                val records = dataManager.getRecordsWithDisplay(account.id, config, account.currentStage)
                val lastRecord = records.lastOrNull()
                val profit = lastRecord?.profit ?: 0.0
                
                dataManager.addProfitRecord(account.name, account.currentStage, profit)
                
                account.currentStage = 0
                dataManager.updateAccount(account)
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    fun showWholeAccountAbandonDialog(account: Account) {
        AlertDialog.Builder(this)
            .setTitle(R.string.abandon)
            .setMessage("将账本「${account.name}」第${account.currentStage}阶段标记为放弃？计划将重置为0阶段。")
            .setPositiveButton(R.string.confirm) { _, _ ->
                val config = dataManager.getConfig()
                val records = dataManager.getRecordsWithDisplay(account.id, config, account.currentStage)
                val lastRecord = records.lastOrNull()
                val principal = lastRecord?.principal ?: 0.0
                
                dataManager.addAbandonRecord(account.name, account.currentStage, principal)
                
                account.currentStage = 0
                dataManager.updateAccount(account)
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
