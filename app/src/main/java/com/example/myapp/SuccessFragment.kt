import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.HomeScreenFragment
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
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
    private lateinit var date: String
    private lateinit var time:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        statusPass()
        val rootView = inflater.inflate(R.layout.fragment_payment_success, container, false)

        val buttonDownloadPdf: Button = rootView.findViewById(R.id.button_download_pdf)
        val buttonBackToHome: Button = rootView.findViewById(R.id.button_back_to_home)

        (requireActivity() as MainActivity).showBottomNavigation(false)

        paymentId = arguments?.getString("paymentId").toString()
        orderid = generateUniqueOrderId()
        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()).toString()
        time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()).toString()
        processOrderAndClearCart()
        buttonDownloadPdf.setOnClickListener {
            generateOrderPdf()
        }

        // Navigate to home screen
        buttonBackToHome.setOnClickListener {
            val homeFragment = HomeScreenFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit()
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
                                Log.d("Product Info", "Name: $productName, Price: $productPrice, Quantity: $productQuantity")
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
                            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                            "time" to SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
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

                val pdfWriter = PdfWriter(FileOutputStream(file))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                // Get current date and time

                // Add Title
                val title = Paragraph("Order Details")
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18f)
                document.add(title)


                val Datetime = Paragraph()
                    .add("Date: $date\n")
                    .add("Time: $time")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10f)
                document.add(Datetime)

                val orderInfo = Paragraph()
                    .add("Payment ID: $paymentId\n")
                    .add("Order ID: $orderid\n")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10f)
                document.add(orderInfo)

                val table = Table(floatArrayOf(3f, 2f, 2f, 3f))
                    .setWidth(UnitValue.createPercentValue(100f)) // Correct method for setting width

                table.addHeaderCell(Paragraph("Product Name").setBold())
                table.addHeaderCell(Paragraph("Quantity").setBold())
                table.addHeaderCell(Paragraph("Price/Unit Rs.").setBold())
                table.addHeaderCell(Paragraph("Total Price Rs.").setBold())

                val userId = auth.currentUser?.uid
                var grandTotal = 0

                if (userId != null) {
                    val querySnapshot = withContext(Dispatchers.IO) {
                        firestore.collection("order_history")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("orderId",orderid)
                            .get()
                            .await()
                    }

                    if (!querySnapshot.isEmpty) {
                        val order = querySnapshot.documents[0]
                        val products = order["products"] as List<Map<String, Any>>

                        // Add product rows to the table
                        products.forEach { product ->
                            val name = product["name"].toString()
                            val quantity = product["quantity"].toString().toInt()
                            val pricePerUnit = product["price"].toString().toInt()
                            val totalPrice = quantity * pricePerUnit
                            grandTotal += totalPrice

                            table.addCell(name)
                            table.addCell(quantity.toString())
                            table.addCell(pricePerUnit.toString())
                            table.addCell(totalPrice.toString())
                        }

                        document.add(Paragraph("Products").setBold().setFontSize(14f).setMarginBottom(5f).setTextAlignment(TextAlignment.CENTER))
                        document.add(table)

                        val tax = (grandTotal * 0).toInt()
                        val totalWithTax = grandTotal + tax

                        val totalParagraph = Paragraph()
                            .add("Grand Total: $grandTotal Rs.\n")
                            .add("Tax (Included): $tax Rs.\n")
                            .add("Total Amount: $totalWithTax Rs.\n")
                            .add("Delivery charges: Free")
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setMarginTop(10f)
                        document.add(totalParagraph)

                        val footer = Paragraph("Paws & Fins Invoice")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)
                            .setMarginTop(20f)
                        document.add(footer)

                        document.close()

                        withContext(Dispatchers.Main) {
                            activity?.let { context ->
                                Toast.makeText(context, "PDF saved at ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                            }

                            // Trigger the download
                            triggerDownload(requireContext(), file.absolutePath)
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
    private fun statusPass(){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("payment_history")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "PENDING") // Assuming the initial status is Pending
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.update("status", "Success")
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Payment status updated to Failed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error updating payment status: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error fetching payment records: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

}