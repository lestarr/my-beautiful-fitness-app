package com.fitness.tracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object EmailHelper {

    fun sendEmailWithAttachment(
        context: Context,
        recipientEmail: String,
        subject: String,
        body: String,
        attachmentFile: File
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                attachmentFile
            )

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(emailIntent, "Send email with...")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
