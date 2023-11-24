package com.brij1999.vitaarth.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class Template (
    var id: String = "",
    var nature: String = "",
    var type: String = "",
    var account: String = "",
    var regex: String = "",
) {

    companion object {
        private const val TAG = "Template"
        private const val collectionName = "templates"
        private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

        suspend fun assimilate(smsSender: String, smsMessage: String, smsTimestamp: Long): Transaction? {
            val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault())
            val formattedDate = sdf.format(Date(smsTimestamp))
            Log.d(TAG, "assimilate: [$formattedDate]\tMessage from $smsSender -> $smsMessage")

            for (template: Template in fetchAll()) {
                if (!template.matches(smsSender, smsMessage))    continue
                val transaction = template.parse(smsSender, smsMessage, smsTimestamp)
                transaction.save()
                Log.d(TAG, "assimilate: Result -> $transaction")
                return transaction
            }
            Log.d(TAG, "assimilate: Result -> null")
            return null
        }

        suspend fun fetchAll(): List<Template> {
            val querySnapshot = firestore.collection(collectionName).get().await()
            return querySnapshot.documents.mapNotNull { document ->
                document.toObject(Template::class.java)
            }
        }
    }

    override fun toString(): String {
        return """Template(
        |   id='$id', 
        |   nature='$nature', 
        |   account='$account', 
        |   regex='$regex'
        )""".trimMargin()
    }

    private fun matches(smsSender: String, smsMessage: String): Boolean {
        val searchStr = "${smsSender}<\\>${smsMessage}"
        return Regex(regex).matches(searchStr)
    }

    private fun parse(smsSender: String, smsMessage: String, smsTimestamp: Long): Transaction {
        val timestamp = Timestamp(smsTimestamp/1000, ((smsTimestamp % 1000) * 1000000).toInt())
        val searchStr = "${smsSender}<\\>${smsMessage}"
        val match = Regex(regex).find(searchStr)
        val sign = when (nature) {
            "CREDIT" -> "+"
            "DEBIT" -> "-"
            else -> ""
        }
        val amt = (sign + match!!.groups["amount"]!!.value).replace(",", "").toDouble()
        val params = mutableMapOf("_raw" to searchStr, "_tid" to id)
        for (groupName in getGroupNames(regex)) {
            val groupValue = match!!.groups[groupName]?.value
            if (groupValue != null) params[groupName] = groupValue
        }

        val transaction = Transaction(
            time = timestamp,
            amount = amt,
            account = account,
            type = if(type!="") type else null,
            extra_params = params,
        )
        return transaction
    }

    private fun getGroupNames(pattern: String): List<String> {
        val groupNames = mutableListOf<String>()

        // Regular expression to match named groups
        val namedGroupPattern = """\(\?<([a-zA-Z][a-zA-Z0-9]*)>"""

        val matcher = Regex(namedGroupPattern).toPattern().matcher(pattern)
        while (matcher.find()) {
            groupNames.add(matcher.group(1)!!)
        }

        return groupNames
    }
}
