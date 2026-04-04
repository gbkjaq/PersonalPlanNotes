package com.example.plannotes.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonSyntaxException

class DataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("plan_notes", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_CONFIG = "config"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_RECORDS_PREFIX = "records_"
    }
    
    fun getConfig(): Config {
        return try {
            val json = prefs.getString(KEY_CONFIG, null)
            if (json != null) {
                gson.fromJson(json, Config::class.java) ?: Config()
            } else {
                Config()
            }
        } catch (e: Exception) {
            Config()
        }
    }
    
    fun saveConfig(config: Config) {
        prefs.edit().putString(KEY_CONFIG, gson.toJson(config)).apply()
    }
    
    fun getAccounts(): List<Account> {
        return try {
            val json = prefs.getString(KEY_ACCOUNTS, null)
            if (json != null) {
                val type = object : TypeToken<List<Account>>() {}.type
                gson.fromJson(json, type) ?: listOf(Account())
            } else {
                listOf(Account())
            }
        } catch (e: Exception) {
            listOf(Account())
        }
    }
    
    fun saveAccounts(accounts: List<Account>) {
        prefs.edit().putString(KEY_ACCOUNTS, gson.toJson(accounts)).apply()
    }
    
    fun addAccount(account: Account) {
        val accounts = getAccounts().toMutableList()
        accounts.add(account)
        saveAccounts(accounts)
    }
    
    fun updateAccount(account: Account) {
        val accounts = getAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            account.updateTime = System.currentTimeMillis()
            accounts[index] = account
            saveAccounts(accounts)
        }
    }
    
    fun deleteAccount(accountId: String) {
        val accounts = getAccounts().toMutableList()
        accounts.removeAll { it.id == accountId }
        if (accounts.isEmpty()) {
            accounts.add(Account())
        }
        saveAccounts(accounts)
        prefs.edit().remove(KEY_RECORDS_PREFIX + accountId).apply()
    }
    
    fun getRecords(accountId: String): List<Record> {
        return try {
            val json = prefs.getString(KEY_RECORDS_PREFIX + accountId, null)
            if (json != null) {
                val type = object : TypeToken<List<Record>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveRecords(accountId: String, records: List<Record>) {
        prefs.edit().putString(KEY_RECORDS_PREFIX + accountId, gson.toJson(records)).apply()
    }
    
    fun addRecord(accountId: String, record: Record) {
        val records = getRecords(accountId).toMutableList()
        records.add(record)
        saveRecords(accountId, records)
    }
    
    fun updateRecord(accountId: String, record: Record) {
        val records = getRecords(accountId).toMutableList()
        val index = records.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            records[index] = record
            saveRecords(accountId, records)
        }
    }
    
    fun deleteRecord(accountId: String, recordId: String) {
        val records = getRecords(accountId).toMutableList()
        records.removeAll { it.id == recordId }
        saveRecords(accountId, records)
    }
    
    fun getRecordsWithDisplay(accountId: String, config: Config): List<RecordDisplay> {
        val account = getAccounts().find { it.id == accountId }
        val quantity = account?.quantity ?: 1
        val coefficient = config.coefficient
        val currentStage = account?.currentStage ?: 1
        
        val records = getRecords(accountId)
        val sortedRecords = records.filter { it.stage <= currentStage }.sortedBy { it.createTime }
        
        var runningPrincipal = 0.0
        return sortedRecords.mapIndexed { index, record ->
            val principal = if (index == 0) {
                record.amount * quantity
            } else {
                record.amount * quantity + runningPrincipal
            }
            val profit = record.amount * coefficient
            val totalProfit = profit - principal
            runningPrincipal = principal
            
            RecordDisplay(
                index = index + 1,
                record = record,
                principal = principal,
                profit = profit,
                totalProfit = totalProfit,
                isProfit = record.status == Record.STATUS_PROFIT,
                isAbandon = record.status == Record.STATUS_ABANDON
            )
        }
    }
    
    fun getAccountWithRecords(accountId: String): AccountWithRecords {
        val accounts = getAccounts()
        val account = accounts.find { it.id == accountId } ?: accounts.firstOrNull() ?: Account()
        val config = getConfig()
        val records = getRecordsWithDisplay(accountId, config)
        return AccountWithRecords(account, records)
    }
    
    fun getAccountSummaries(): Map<String, AccountSummary> {
        val accounts = getAccounts()
        val config = getConfig()
        val summaries = mutableMapOf<String, AccountSummary>()
        
        for (account in accounts) {
            val records = getRecordsWithDisplay(account.id, config)
            val lastRecord = records.lastOrNull()
            summaries[account.id] = AccountSummary(
                principal = lastRecord?.principal ?: 0.0,
                totalProfit = lastRecord?.totalProfit ?: 0.0,
                recordCount = records.size
            )
        }
        
        return summaries
    }
}
