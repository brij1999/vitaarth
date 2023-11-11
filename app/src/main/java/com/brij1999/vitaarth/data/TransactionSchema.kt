package com.brij1999.vitaarth.data

import com.google.firebase.Timestamp


data class Transaction (
    var id: String? = null,
    var time: Timestamp? = null,
    var type: String? = null,
    var amount: Double? = null,
    var account: String? = null,
    var description: String? = null,
    var extra_params: Map<String, String>? = null,
)
