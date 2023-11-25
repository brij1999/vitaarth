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
    var sourceTag: String,
    var extra_params: Map<String, String>? = null,
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
        return "$output\n)"
    }

    suspend fun save() {
        val documentRef = if (id == null) {
            firestore.collection(collectionName).document()
        } else {
            firestore.collection(collectionName).document(id!!)
        }
        if (id == null)    id = documentRef.id
        documentRef.set(this).await()
    }
}
