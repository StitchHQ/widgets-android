package com.stitch.cardmanagement.utilities

object CardUtils {

    fun getCardNumber(cardNumber: String): String {
        return cardNumber.substring(0, 4) + " " +
                cardNumber.substring(4, 8) + " " +
                cardNumber.substring(8, 12) + " " +
                cardNumber.substring(12, 16)
    }
}