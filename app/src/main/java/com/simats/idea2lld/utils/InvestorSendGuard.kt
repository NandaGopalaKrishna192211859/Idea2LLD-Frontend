package com.simats.idea2lld.utils

import android.content.Context
import android.widget.Toast

object InvestorSendGuard {

    // founder side - founder send to investor
    fun showResult(
        context: Context,
        totalSelected: Int,
        sentCount: Int
    ) {
        when {
            sentCount == 0 -> {
                Toast.makeText(
                    context,
                    "Already sent to selected investors",
                    Toast.LENGTH_LONG
                ).show()
            }

            sentCount < totalSelected -> {
                Toast.makeText(
                    context,
                    "Sent to $sentCount investors. ${totalSelected - sentCount} were already sent.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    context,
                    "Sent to all selected investors",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // investor side - investor send to founder - that method
    fun requestByInvestor(
        context: Context,
        isDuplicate: Boolean
    ) {
        if (isDuplicate) {
            Toast.makeText(
                context,
                "Already sent to founder",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "Request sent to founder",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
