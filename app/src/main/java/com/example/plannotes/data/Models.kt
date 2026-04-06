package com.example.plannotes.data

import java.util.UUID

data class Config(
    var quantity: Int = 1,
    var coefficient: Double = 47.0
)

data class Account(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "默认账本",
    var quantity: Int = 1,
    var currentStage: Int = 0,
    var coefficient: Double? = null,
    var remark: String = "",
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
    val profit: Double,
    val totalProfit: Double,
    val recordCount: Int,
    val totalRecordCount: Int
)

data class ReportItem(
    val accountName: String,
    val stage: Int,
    val amount: Double,
    val createTime: Long
)

data class ProfitLossRecord(
    val id: String = UUID.randomUUID().toString(),
    val accountName: String = "",
    val stage: Int = 0,
    val principal: Double = 0.0,
    val type: Int = TYPE_PROFIT,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_PROFIT = 1
        const val TYPE_ABANDON = 2
    }
}
