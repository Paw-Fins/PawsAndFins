import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SuccessFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var paymentId: String
    private lateinit var orderid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_payment_success, container, false)

        val buttonDownloadPdf: Button = rootView.findViewById(R.id.button_download_pdf)
        val buttonBackToHome: Button = rootView.findViewById(R.id.button_back_to_home)

        paymentId = arguments?.getString("paymentId").toString()
        orderid = generateUniqueOrderId()

        processOrderAndClearCart()

        // Button to download the PDF
        buttonDownloadPdf.setOnClickListener {
            generateOrderPdf()
        }

        // Navigate to home screen
        buttonBackToHome.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return rootView
    }

    private fun processOrderAndClearCart() {
        val userId = auth.currentUser?.uid
        userId?.let {

            firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val productList = mutableListOf<Map<String, Any>>()
                        var totalAmount = 0

                        for (document in querySnapshot.documents) {
                            val product = document.data
                            if (product != null) {
                                val productName = product["name"].toString()
                                val productPrice = product["price"].toString().toInt()
                                val productQuantity = product["quantity"].toString().toInt()

                                productList.add(
                                    mapOf(
                                        "name" to productName,
                                        "price" to productPrice,
                                        "quantity" to productQuantity
                                    )
                                )
                                totalAmount += productPrice * productQuantity
                            }
                        }

                        val orderData = mapOf(
                            "userId" to userId,
                            "products" to productList,
                            "totalAmount" to totalAmount,
                            "paymentId" to paymentId,
                            "orderId" to orderid,
                            "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )

                        if (isAdded) {  // Check if fragment is attached before showing Toast or accessing context
                            firestore.collection("order_history").add(orderData)
                                .addOnSuccessListener {
                                    activity?.let { context ->
                                        Toast.makeText(context, "Order history saved successfully!", Toast.LENGTH_SHORT).show()
                                        clearUserCart()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    activity?.let { context ->
                                        Toast.makeText(context, "Error saving order history: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Log.e("SuccessFragment", "Fragment is not attached to the activity.")
                        }

                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded) {
                        activity?.let { context ->
                            Toast.makeText(context, "Error fetching cart: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun clearUserCart() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("orders").whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        document.reference.delete()
                    }
                    activity?.let { context ->
                        Toast.makeText(context, "Cart cleared successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    activity?.let { context ->
                        Toast.makeText(context, "Error clearing cart: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun generateOrderPdf() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = "OrderInvoice_${orderid}.pdf"
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                // Ensure the directory exists
                if (!file.exists()) {
                    file.parentFile?.mkdirs()
                }

                // Initialize PDF writer and document
                val pdfWriter = PdfWriter(FileOutputStream(file))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.add(Paragraph("Order Details").setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))

                val userId = auth.currentUser?.uid

                if (userId != null) {
                    try {
                        val querySnapshot = withContext(Dispatchers.IO) {
                            firestore.collection("order_history")
                                .whereEqualTo("userId", userId)
                                .get()
                                .await()
                        }

                        if (!querySnapshot.isEmpty) {
                            val order = querySnapshot.documents[0]
                            val products = order["products"] as List<Map<String, Any>>

                            // Add product details to the PDF
                            products.forEach { product ->
                                val name = product["name"]
                                val price = product["price"]
                                val quantity = product["quantity"]
                                document.add(Paragraph("Product: $name, Price: ₹$price, Quantity: $quantity"))
                            }

                            val totalAmount = order["totalAmount"]
                            val paymentId = order["paymentId"]
                            val date = order["date"]
                            val orderid = order["orderId"]

                            document.add(Paragraph("\nTotal Amount: ₹$totalAmount"))
                            document.add(Paragraph("Payment ID: $paymentId"))
                            document.add(Paragraph("Order ID: $orderid"))
                            document.add(Paragraph("Date: $date"))

                            document.close()

                            // Switch to main thread to show Toast and trigger the download
                            withContext(Dispatchers.Main) {
                                activity?.let { context ->
                                    Toast.makeText(context, "PDF saved at ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                                }

                                // Trigger the download
                                triggerDownload(requireContext(), file.absolutePath)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e("SuccessFragment", "Error fetching data from Firestore", e)
                            Toast.makeText(requireContext(), "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SuccessFragment", "Error generating PDF", e)
                    Toast.makeText(requireContext(), "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun generateUniqueOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val randomString = (1..6).map { ('A'..'Z').random() }.joinToString("")
        return "ORD_${timestamp}_$randomString"
    }
}
