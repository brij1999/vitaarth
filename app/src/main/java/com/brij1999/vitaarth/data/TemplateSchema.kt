package com.brij1999.vitaarth.data

import android.telephony.SmsMessage
import com.google.firebase.Timestamp

class Template (
    var id: String = "",
    var nature: String = "",
    var type: String = "",
    var account: String = "",
    var regex: String = "",
) {
    fun matches(sms: SmsMessage): Boolean {
        val smsSender = sms.originatingAddress.toString()
        val smsMessage = sms.messageBody
        val searchStr = "${smsSender}<\\>${smsMessage}"
        return Regex(regex).matches(searchStr)
    }

    fun parse(sms: SmsMessage): Transaction {
        val smsTimestamp = Timestamp(sms.timestampMillis/1000, ((sms.timestampMillis % 1000) * 1000000).toInt())
        val smsSender = sms.originatingAddress.toString()
        val smsMessage = sms.messageBody
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
            time = smsTimestamp,
            amount = amt,
            account = account,
            type = if(type!="") type else null,
            extra_params = params,
        )
        return transaction
    }

    override fun toString(): String {
        return "Template(id='$id', nature='$nature', account='$account', regex='$regex')"
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
