package com.stitch.cardmanagement.data.model.response

import com.google.gson.annotations.SerializedName

data class WidgetsSecureSessionKeyResponse(
    @SerializedName("token")
    var generatedKey: String,
    @SerializedName("key")
    var key: String,
)