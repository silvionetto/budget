package com.silvionetto.budget

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

enum class BudgetType {
    EXPENSE,
    INCOME
}

enum class TransactionSide {
    Debit,
    Credit
}

@MappedSuperclass
open class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = 0,
    @Version var version: Long? = 0,
    @Temporal(TemporalType.DATE) var lastUpdateDate: Date = Date()
) : Serializable

@Entity
@Table(name = "users")
data class User(
        var login: String,
        var firstname: String,
        var lastname: String,
        var description: String? = null
) : BaseEntity()

@Entity
data class BudgetCategory(
        @Column(unique = true) var name: String,
        @Enumerated(EnumType.STRING) var type: BudgetType
) : BaseEntity()

@Entity
data class BudgetSubCategory(
        var name: String,
        @ManyToOne
        @JoinColumn(name = "category_id")
        var category: BudgetCategory
) : BaseEntity()

@Entity
data class Store(
        @Column(unique = true) var name: String,
        @ManyToOne
        var subCategory: BudgetSubCategory
) : BaseEntity()

@Entity
@Table(name = "budget_transactions")
data class Transaction(
        var date: Date,
        @ManyToOne var store: Store,
        var account: String,
        var contraAccount: String,
        var code: String,
        var debitCredit: String,
        var amount: Double,
        var transactionType: String,
        @Column(name = "notifications", length = 512)
        var notifications: String,
        @ManyToOne var subCategory: BudgetSubCategory
) : BaseEntity()

@Entity
data class Balance(
        var date: Date,
        var openingBalance: Double
) : BaseEntity()

@Entity
@Table(name = "uploaded_documents")
data class UploadedDocument(
        @Column(nullable = false, length = 320) var ownerEmail: String,
        @Column(nullable = false, length = 255) var fileName: String,
        @Column(nullable = false, length = 100) var contentType: String,
        @Column(nullable = false) var fileSize: Long,
        @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var uploadedAt: Date,
        @Column(nullable = false) var content: ByteArray
) : BaseEntity()

class Budget(
       val category: String,
       var january: Double,
       var february: Double,
       var march: Double,
       var april: Double,
       var may: Double,
       var june: Double,
       var july: Double,
       var august: Double,
       var september: Double,
       var october: Double,
       var november: Double,
       var december: Double
) {
    fun getTotal(): Double {
        val months = arrayOf(january,
                february,
                march,
                april,
                may,
                june,
                july,
                august,
                september,
                october,
                november,
                december)

        return months.sum()

    }

    fun getAverage(): Double {
        val months = arrayOf(january,
                february,
                march,
                april,
                may,
                june,
                july,
                august,
                september,
                october,
                november,
                december)

        return months.sum() / 12
    }
}