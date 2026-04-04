package com.example.plannotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.data.Config
import com.google.gson.Gson
import org.json.JSONObject

class SettingsFragment : Fragment() {
    
    private var etQuantity: EditText? = null
    private var etCoefficient: EditText? = null
    private var tvCurrentConfig: TextView? = null
    private var btnExport: Button? = null
    private var btnImport: Button? = null
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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        etQuantity = view.findViewById(R.id.et_quantity)
        etCoefficient = view.findViewById(R.id.et_coefficient)
        tvCurrentConfig = view.findViewById(R.id.tv_current_config)
        val btnSave: Button? = view.findViewById(R.id.btn_save)
        btnExport = view.findViewById(R.id.btn_export)
        btnImport = view.findViewById(R.id.btn_import)
        
        loadConfig()
        
        btnSave?.setOnClickListener {
            saveConfig()
        }
        
        btnExport?.setOnClickListener {
            exportLauncher.launch("plan_notes_backup.json")
        }
        
        btnImport?.setOnClickListener {
            showImportConfirmDialog()
        }
    }
    
    private fun loadConfig() {
        val activity = activity as? MainActivity ?: return
        val config = activity.dataManager.getConfig()
        etQuantity?.setText(config.quantity.toString())
        etCoefficient?.setText(config.coefficient.toString())
        updateCurrentConfigText(config)
    }
    
    private fun updateCurrentConfigText(config: Config) {
        tvCurrentConfig?.text = getString(R.string.current_config, config.quantity, config.coefficient)
    }
    
    private fun saveConfig() {
        val activity = activity as? MainActivity ?: return
        
        val quantityStr = etQuantity?.text.toString() ?: ""
        val coefficientStr = etCoefficient?.text.toString() ?: ""
        
        if (quantityStr.isEmpty() || coefficientStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }
        
        val quantity = quantityStr.toIntOrNull()
        val coefficient = coefficientStr.toIntOrNull()
        
        if (quantity == null || coefficient == null || quantity <= 0 || coefficient <= 0) {
            Toast.makeText(requireContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show()
            return
        }
        
        val config = Config(quantity = quantity, coefficient = coefficient)
        activity.dataManager.saveConfig(config)
        updateCurrentConfigText(config)
        
        Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
    }
    
    private fun showImportConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.import_data)
            .setMessage(R.string.import_confirm)
            .setPositiveButton(R.string.confirm) { _, _ ->
                importLauncher.launch(arrayOf("application/json"))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun exportData(uri: android.net.Uri) {
        val activity = activity as? MainActivity ?: return
        try {
            val config = activity.dataManager.getConfig()
            val accounts = activity.dataManager.getAccounts()
            val recordsMap = mutableMapOf<String, List<com.example.plannotes.data.Record>>()
            
            for (account in accounts) {
                recordsMap[account.id] = activity.dataManager.getRecords(account.id)
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
            
            requireContext().contentResolver.openOutputStream(uri)?.use { output ->
                output.write(backup.toString().toByteArray())
            }
            
            Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.export_failed, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun importData(uri: android.net.Uri) {
        val activity = activity as? MainActivity ?: return
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: throw Exception("Cannot read file")
            
            val backup = JSONObject(json)
            
            val configObj = backup.getJSONObject("config")
            val config = Config(
                quantity = configObj.getInt("quantity"),
                coefficient = configObj.getInt("coefficient")
            )
            activity.dataManager.saveConfig(config)
            
            val accountsJson = backup.getString("accounts")
            val accounts = gson.fromJson(accountsJson, Array<com.example.plannotes.data.Account>::class.java).toList()
            activity.dataManager.saveAccounts(accounts)
            
            val recordsJson = backup.getString("records")
            val recordsMap = gson.fromJson(recordsJson, Map::class.java) as Map<String, List<com.example.plannotes.data.Record>>
            
            for ((accountId, records) in recordsMap) {
                activity.dataManager.saveRecords(accountId, records)
            }
            
            Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
