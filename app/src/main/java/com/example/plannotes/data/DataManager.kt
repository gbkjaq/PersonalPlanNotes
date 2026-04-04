package com.example.plannotes.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("plan_notes", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_CONFIG = "config"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_RECORDS_PREFIX = "records_"
    }
    
    fun getConfig(): Config {
        val json = prefs.getString(KEY_CONFIG, null)
        return if (json != null) {
            gson.fromJson(json, Config::class.java)
        } else {
            Config()
        }
    }
    
    fun saveConfig(config: Config) {
        prefs.edit().putString(KEY_CONFIG, gson.toJson(config)).apply()
    }
    
    fun getAccounts(): List<Account> {
        val json = prefs.getString(KEY_ACCOUNTS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Account>>() {}.type
            gson.fromJson(json, type)
        } else {
            val defaultAccount = Account()
            listOf(defaultAccount)
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
        val json = prefs.getString(KEY_RECORDS_PREFIX + accountId, null)
        return if (json != null) {
            val type = object : TypeToken<List<Record>>() {}.type
            gson.fromJson(json, type)
        } else {
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
        val records = getRecords(accountId)
        val sortedRecords = records.sortedBy { it.createTime }
        
        var runningPrincipal = 0.0
        return sortedRecords.mapIndexed { index, record ->
            val principal = if (index == 0) {
                record.amount * config.quantity
            } else {
                record.amount * config.quantity + runningPrincipal
            }
            val profit = record.amount * config.coefficient
            val totalProfit = profit - principal
            runningPrincipal = principal
            
            RecordDisplay(
                index = index + 1,
                record = record,
                principal = principal,
                profit = profit,
                totalProfit = totalProfit
            )
        }
    }
    
    fun getAccountWithRecords(accountId: String): AccountWithRecords {
        val account = getAccounts().find { it.id == accountId } ?: Account()
        val config = getConfig()
        val records = getRecordsWithDisplay(accountId, config)
        return AccountWithRecords(account, records)
    }
}
