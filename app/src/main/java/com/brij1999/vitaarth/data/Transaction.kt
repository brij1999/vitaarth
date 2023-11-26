package com.brij1999.vitaarth.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class Transaction (
    var id: String? = null,
    var time: Timestamp? = null,
    var type: String? = null,
    var amount: Double? = null,
    var account: String? = null,
    var description: String? = null,
    var sourceTag: String? = "",
    var extra_params: Map<String, String>? = null,
    var createdAt: Timestamp? = null,
    var updateAt: Timestamp? = null,
    var changeLog: MutableList<MutableMap<String, *>>? = mutableListOf(),
) {
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
        var output = """
        Transaction(
        |   id='$id', 
        |   time='$time',
        |   type='$type',
        |   amount='$amount',
        |   account='$account',
        |   description='$description',
        |   sourceTag='$sourceTag',
        """.trimIndent()
        if (extra_params!=null) {
            extra_params!!.forEach { (key, value) ->
                output+="\n|        extra_params.$key -> $value"
            }
        }
        output+="\n"+""""
        |   createdAt='$createdAt',
        |   updatedAt='$updateAt',
        """.trimIndent()
        return "$output\n)"
    }

    suspend fun save(tag: String): Transaction {
        val now = System.currentTimeMillis()
        val timestamp = Timestamp(now/1000, ((now % 1000) * 1000000).toInt())

        val documentRef = if (id == null) {
            firestore.collection(collectionName).document()
        } else {
            firestore.collection(collectionName).document(id!!)
        }

        if (id == null) {
            id = documentRef.id
            createdAt = timestamp
        }

        sourceTag=tag
        updateAt = timestamp
        updateChangeLog(tag, now)
        documentRef.set(this).await()
        return this
    }

    private fun updateChangeLog(tag: String, now: Long) {
         val event = mutableMapOf("tag" to tag, "time" to Timestamp(now/1000, ((now % 1000) * 1000000).toInt()))
        changeLog!!.add(event)
    }
}
