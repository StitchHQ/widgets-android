package com.stitch.cardmanagement.data.model.request

import com.google.gson.annotations.SerializedName

class WidgetsSecureSessionKeyRequest(
    var token: String,
    @SerializedName("deviceFingerprint")
    var deviceFingerprint: String,
)