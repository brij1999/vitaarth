package com.brij1999.vitaarth.io

import com.brij1999.vitaarth.data.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object TransactionManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private const val collectionName = "transactions"

    suspend fun addTransaction(transaction: Transaction): Transaction {
        val documentRef = firestore.collection(collectionName).document()
        transaction.id = documentRef.id
        documentRef.set(transaction).await()
        return transaction
    }

    suspend fun getTransaction(transactionId: String): Transaction? {
        val documentSnapshot = firestore.collection(collectionName).document(transactionId).get().await()
        return documentSnapshot.toObject(Transaction::class.java)
    }

    suspend fun getAllTransactions(): List<Transaction> {
        val querySnapshot = firestore.collection(collectionName).get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Transaction::class.java)
        }
    }

    suspend fun deleteTransactionById(transactionId: String) {
        firestore.collection(collectionName).document(transactionId).delete().await()
    }
}
