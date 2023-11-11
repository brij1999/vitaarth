package com.brij1999.vitaarth.io

import com.brij1999.vitaarth.data.Template
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object TemplateManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private const val collectionName = "templates"

    suspend fun addTemplate(template: Template): Template {
        val documentRef = firestore.collection(collectionName).document()
        template.id = documentRef.id
        documentRef.set(template).await()
        return template
    }

    suspend fun getTemplate(templateId: String): Template? {
        val documentSnapshot = firestore.collection(collectionName).document(templateId).get().await()
        return documentSnapshot.toObject(Template::class.java)
    }

    suspend fun getAllTemplates(): List<Template> {
        val querySnapshot = firestore.collection(collectionName).get().await()
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Template::class.java)
        }
    }
}