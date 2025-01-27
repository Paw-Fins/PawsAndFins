import android.content.Context
import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

fun triggerDownload(context: Context, filePath: String) {
    val file = File(filePath)

    if (!file.exists()) {
        Toast.makeText(context, "File not found: $filePath", Toast.LENGTH_SHORT).show()
        return
    }

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // Ensure this matches your manifest authority
        file
    )

    try {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (openIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(openIntent)
        } else {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}