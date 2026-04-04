package com.example.plannotes.data

import java.util.UUID

data class Config(
    var quantity: Int = 1,
    var coefficient: Int = 47
)

data class Account(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "默认账本",
    var quantity: Int = 1,
    var currentStage: Int = 1,
    val createTime: Long = System.currentTimeMillis(),
    var updateTime: Long = System.currentTimeMillis()
)

data class Record(
    val id: String = UUID.randomUUID().toString(),
    var amount: Double = 0.0,
    var stage: Int = 1,
    var remark: String = "",
    val createTime: Long = System.currentTimeMillis(),
    var status: Int = STATUS_NORMAL
) {
    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_PROFIT = 1
        const val STATUS_ABANDON = 2
    }
}

data class RecordDisplay(
    val index: Int,
    val record: Record,
    val principal: Double,
    val profit: Double,
    val totalProfit: Double,
    val isProfit: Boolean = false,
    val isAbandon: Boolean = false
)

data class AccountWithRecords(
    val account: Account,
    val records: List<RecordDisplay>
)

data class AccountSummary(
    val principal: Double,
    val totalProfit: Double,
    val recordCount: Int
)

data class ReportItem(
    val accountName: String,
    val record: Record
)
