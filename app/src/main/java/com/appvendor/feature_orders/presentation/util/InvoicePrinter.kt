package com.appvendor.feature_orders.presentation.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.appvendor.feature_orders.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePrinter {

    fun printInvoice(
        context: Context,
        order: Order,
        shopName: String,
        address: String = "",
        phone: String = "",
        email: String = "",
        gstNumber: String = "",
        paperWidth: String = "80mm",
        printerType: String = "BROWSER"
    ) {
        if (printerType.equals("BLUETOOTH", ignoreCase = true)) {
            // TODO: Route to ESC/POS Bluetooth implementation
            android.widget.Toast.makeText(context, "Routing to Bluetooth Printer...", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val htmlContent = generateInvoiceHtml(order, shopName, address, phone, email, gstNumber, paperWidth)
        
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = view.createPrintDocumentAdapter("Invoice_${order.queueNumber}")
                val jobName = "Invoice_${order.queueNumber}"
                
                val mediaSize = when (paperWidth) {
                    "80mm" -> PrintAttributes.MediaSize("ROLL_80", "80mm Roll", 3150, 11811)
                    "58mm" -> PrintAttributes.MediaSize("ROLL_58", "58mm Roll", 2283, 11811)
                    else -> PrintAttributes.MediaSize.ISO_A4
                }
                
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(mediaSize)
                        .build()
                )
            }
        }
        
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    private fun generateInvoiceHtml(
        order: Order,
        shopName: String,
        address: String,
        phone: String,
        email: String,
        gstNumber: String,
        paperWidth: String
    ): String {
        // Calculate subtotal
        val subtotal = order.items.sumOf { it.totalPrice }
        
        // Items HTML
        val itemsHtml = order.items.joinToString("") { item ->
            val customizationsStr = if (item.selectedCustomizations.isNotEmpty()) {
                val names = item.selectedCustomizations.flatMap { it.selectedOptions }.joinToString(", ") { it.name }
                "<br><small style=\"color:#666\">+${escapeHtml(names)}</small>"
            } else ""

            """
            <tr>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;">
                  ${escapeHtml(item.menuItemName)}
                  $customizationsStr
                  ${if (!item.specialInstructions.isNullOrBlank()) "<br><small style=\"color:#666\">Note: ${escapeHtml(item.specialInstructions)}</small>" else ""}
              </td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:center;">${item.quantity}</td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:right;">₹${String.format(Locale.US, "%.2f", item.unitPrice)}</td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:right;">₹${String.format(Locale.US, "%.2f", item.totalPrice)}</td>
            </tr>
            """.trimIndent()
        }

        val date = com.appvendor.core.util.DateUtils.formatToIstReadableTime(order.createdAt)

        // Metadata
        val metadataHtml = if (order.metadata != null && order.metadata.isNotEmpty()) {
            "<div class=\"divider\"></div><div class=\"order-meta\">" + 
            order.metadata.entries.joinToString("") { "<div><strong>${escapeHtml(it.key)}:</strong> ${escapeHtml(it.value)}</div>" } + 
            "</div>"
        } else ""

        val taxName = order.taxName ?: "Tax"
        val taxValue = order.taxAmount ?: 0.0
        val chargeName = order.serviceChargeName ?: "Service Charge"
        val chargeAmt = order.serviceChargeAmount ?: 0.0
        val couponName = if (!order.couponCode.isNullOrBlank()) "Coupon (${order.couponCode})" else "Coupon Discount"
        val couponDiscount = order.couponDiscount ?: 0.0
        
        val computedTotal = order.totalAmount

        val maxWidth = if (paperWidth == "58mm") "220px" else "350px"
        val fontSize = if (paperWidth == "58mm") "12px" else "14px"
        val padding = if (paperWidth == "58mm") "10px" else "20px"

        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8">
              <title>Invoice — ${escapeHtml(shopName)}</title>
              <style>
                body{font-family:'Courier New', Courier, monospace;max-width:$maxWidth;margin:0 auto;padding:$padding;color:#000;font-size:$fontSize;line-height:1.4}
                h2{text-align:center;margin:0 0 5px 0;font-size:22px;text-transform:uppercase}
                .info{text-align:center;margin-bottom:15px;font-size:12px}
                .divider{border-top:1px dashed #000;margin:10px 0}
                .order-meta{margin-bottom:15px;font-size:13px}
                table{width:100%;border-collapse:collapse;margin-bottom:15px;font-size:13px}
                th{text-align:left;border-bottom:1px solid #000;padding-bottom:5px}
                .totals-table{width:100%;font-size:13px}
                .totals-table td{padding:3px 0}
                .totals-table .bold{font-weight:bold}
                .totals-table .grand-total{font-size:18px;font-weight:bold;border-top:1px dashed #000;padding-top:8px;margin-top:5px}
                .footer{text-align:center;font-size:11px;margin-top:20px}
                .instruction{font-size:12px;margin-top:10px;font-style:italic;}
                @media print{body{max-width:100%;padding:0;} .no-print{display:none}}
              </style></head><body>
              <h2>${escapeHtml(shopName)}</h2>
              <div class="info">
                ${if (address.isNotBlank()) address + "<br>" else ""}
                ${if (phone.isNotBlank()) "Ph: " + phone + "<br>" else ""}
                ${if (email.isNotBlank()) email + "<br>" else ""}
                ${if (gstNumber.isNotBlank()) "GSTIN: " + escapeHtml(gstNumber) else ""}
              </div>
              
              <div class="divider"></div>
              
              <div class="order-meta">
                <div><strong>Order #:</strong> ${order.queueNumber}</div>
                <div><strong>Date:</strong> $date</div>
              </div>
              
              $metadataHtml
              
              <div class="divider"></div>
              
              <table>
                <thead><tr>
                  <th>Item</th><th style="text-align:center">Qty</th>
                  <th style="text-align:right">Price</th><th style="text-align:right">Total</th>
                </tr></thead>
                <tbody>${itemsHtml}</tbody>
              </table>
              
              <table class="totals-table">
                <tr>
                    <td>Subtotal</td>
                    <td style="text-align:right">₹${String.format(Locale.US, "%.2f", subtotal)}</td>
                </tr>
                ${if (couponDiscount > 0) """
                <tr>
                    <td>${escapeHtml(couponName)}</td>
                    <td style="text-align:right">-₹${String.format(Locale.US, "%.2f", couponDiscount)}</td>
                </tr>
                """.trimIndent() else ""}
                ${if (taxValue > 0) """
                <tr>
                    <td>${escapeHtml(taxName)}</td>
                    <td style="text-align:right">₹${String.format(Locale.US, "%.2f", taxValue)}</td>
                </tr>
                """.trimIndent() else ""}
                ${if (chargeAmt > 0) """
                <tr>
                    <td>${escapeHtml(chargeName)}</td>
                    <td style="text-align:right">₹${String.format(Locale.US, "%.2f", chargeAmt)}</td>
                </tr>
                """.trimIndent() else ""}
                <tr>
                    <td class="grand-total" style="padding-top:10px">Total</td>
                    <td class="grand-total" style="text-align:right;padding-top:10px">₹${String.format(Locale.US, "%.2f", computedTotal)}</td>
                </tr>
              </table>
              
              ${if (!order.customerNote.isNullOrBlank()) "<div class=\"instruction\"><strong>Instruction:</strong> ${escapeHtml(order.customerNote)}</div>" else ""}
              
              <div class="divider"></div>
              
              <div class="footer">
                Thank you for visiting!<br>
                Powered by DeQueue
              </div>
            </body></html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String?): String {
        if (text == null) return ""
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
