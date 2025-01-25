// DownloadUtils.kt
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

fun triggerDownload(context: Context, filePath: String) {
    val file = File(filePath)

    // Check if the file exists
    if (!file.exists()) {
        Toast.makeText(context, "File not found: $filePath", Toast.LENGTH_SHORT).show()
        return
    }

    // Get a content URI using FileProvider
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // Ensure this matches your manifest authority
        file
    )

    // Open the file with an appropriate app
    try {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Check if there's an app to handle the intent
        if (openIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(openIntent)
        } else {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
