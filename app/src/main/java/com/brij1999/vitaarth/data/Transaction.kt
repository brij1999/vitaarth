package com.brij1999.vitaarth.data

import android.util.Log
import com.brij1999.vitaarth.utils.toFormattedString
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date


data class Transaction (
    var id: String? = null,
    var time: Timestamp? = null,
    var type: String? = null,
    var amount: Double? = null,
    var account: String? = null,
    var description: String? = null,
    var sourceTag: String? = "",
    var extraParams: MutableMap<String, String> = mutableMapOf(),
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var changeLog: MutableList<MutableMap<String, *>> = mutableListOf(),
) {
    //TODO: implement "forceCreate" to record missed near-identical transactions

    companion object {
        private const val TAG = "Transaction"
        private const val collectionName = "transactions"
        private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

        suspend fun fetch(transactionId: String): Transaction? {
            val documentSnapshot = firestore.collection(collectionName).document(transactionId).get().await()
            return documentSnapshot.toObject(Transaction::class.java)
        }

        suspend fun fetchAll(): List<Transaction> {
            val querySnapshot = firestore.collection(collectionName).get().await()
            return querySnapshot.documents.mapNotNull { document ->
                document.toObject(Transaction::class.java)
            }
        }
    }

    override fun toString(): String {
        return """
        Transaction:
        |   id: '$id' 
        |   time: '${time?.toDate()?.toString()}'
        |   type: '$type'
        |   amount: '$amount'
        |   account: '$account'
        |   description: '$description'
        |   sourceTag: '$sourceTag'
        |   createdAt: '${createdAt?.toDate()?.toString()}'
        |   updatedAt: '${updatedAt?.toDate()?.toString()}'
        |   extraParams: $extraParams
        """.trimIndent()
    }

    suspend fun save(tag: String): Transaction {
        var existingTransaction: Transaction? = null
        val diff: Long = 2 * 60 * 1000            // 2 minutes
        val now = Timestamp.now()
        val transactionsCollection = firestore.collection(collectionName)

        sourceTag=tag
        updatedAt = now
        updateChangeLog(sourceTag!!, updatedAt!!)

        var query = transactionsCollection
            .whereGreaterThanOrEqualTo("time", Timestamp(Date(time!!.toDate().time - diff)))
            .whereLessThanOrEqualTo("time", Timestamp(Date(time!!.toDate().time + diff)))
            .whereEqualTo("amount", amount)

        if (account!=null)  query = query.whereEqualTo("account", account)
        val res = query.get().await()

        res.documents
            .mapNotNull { document -> document.toObject(Transaction::class.java) }
            .sortedBy { transaction -> transaction.time!!.seconds }
            .forEach { transaction ->
                existingTransaction = transaction
            }

        if (existingTransaction==null) {
            createdAt = now
            id = generateId()
            transactionsCollection.document(id!!).set(this).await()
            Log.d(TAG, "save: Created new transaction -> $id")
        } else {
            Log.d(TAG, "save: found existing match for [ $id ]\t->\t[ ${existingTransaction!!.id} ]")
            updateThisWith(existingTransaction!!)
        }

        return this
    }

    private suspend fun updateThisWith(other: Transaction) {
        val fieldsUpdated = mutableSetOf<String>()
        printDiff(other)

        id = other.id ?: id?.also { fieldsUpdated.add("id") }
        time = other.time ?: time?.also { fieldsUpdated.add("time") }
        type = other.type ?: type?.also { fieldsUpdated.add("type") }
        amount = other.amount ?: amount?.also { fieldsUpdated.add("amount") }
        account = other.account ?: account?.also { fieldsUpdated.add("account") }
        description = other.description ?: description?.also { fieldsUpdated.add("description") }
        createdAt = other.createdAt ?: createdAt?.also { fieldsUpdated.add("createdAt") }


        if (extraParams.isEmpty()) {
            extraParams = other.extraParams
        } else if (other.extraParams.isNotEmpty()) {
            extraParams.putAll(other.extraParams)
        }

        // CAUTION: Review this logic if this fn gets used outside "save()"
        if (fieldsUpdated.isNotEmpty()) {
            Log.d(TAG, "updateThisWith: Updated null values for fields: [${fieldsUpdated.joinToString(separator = ", ")}]")
            other.changeLog.add(changeLog.last())
            changeLog = other.changeLog
            firestore.collection(collectionName).document(id!!).set(this).await()
        } else {
            sourceTag = other.sourceTag ?: sourceTag
            updatedAt = other.updatedAt ?: updatedAt
            changeLog = other.changeLog.ifEmpty { changeLog }
        }
    }

    private fun printDiff(other: Transaction) {
        val fields = Transaction::class.members
            .filterIsInstance<java.lang.reflect.Field>()
            .filter { it.name != "changeLog" } // Exclude changeLog from comparison

        // Check if there are any differences
        val hasDifferences = fields.any { field ->
            field.isAccessible = true
            val thisValue = field.get(this)
            val otherValue = field.get(other)
            thisValue != otherValue
        }

        Log.d(TAG, "printDiff: [ $id ]    v/s    [ ${other.id} ]")
        if (hasDifferences) {
            Log.d(TAG, "printDiff: ------------: <Transaction Diff> :------------")
            for (field in fields) {
                field.isAccessible = true
                val thisValue = field.get(this)
                val otherValue = field.get(other)

                if (thisValue != otherValue) {
                    Log.d(TAG, "printDiff: ${field.name}:")
                    Log.d(TAG, "printDiff:     This: $thisValue")
                    Log.d(TAG, "printDiff:     Other: $otherValue")
                }
            }
            Log.d(TAG, "printDiff: ------------: </Transaction Diff> :------------")
        } else {
            Log.d(TAG, "printDiff: ------------: <no difference found> :------------")
        }
    }


    private fun updateChangeLog(tag: String, time: Timestamp) {
        val event = mutableMapOf("tag" to tag, "time" to time)
        changeLog.add(event)
    }

    private fun generateId(): String {
        return "["+time!!.toDate().toFormattedString("yyyy-MM-dd HH:mm:ss")+"] | ["+createdAt!!.toDate().toFormattedString("yyyy-MM-dd HH:mm:ss")+"]"
    }
}
