package com.appvendor.feature_orders.domain.util

import com.appvendor.feature_orders.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoiceGenerator {

    private fun escapeHtml(text: String?): String {
        if (text == null) return ""
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun generateHtml(order: Order, shopName: String, address: String = "", phone: String = "", email: String = "", gstNumber: String = ""): String {
        val safeShopName = escapeHtml(shopName.ifBlank { "DeQueue Shop" })
        val safeAddress = escapeHtml(address)
        val safePhone = escapeHtml(phone)
        val safeEmail = escapeHtml(email)
        val safeGst = escapeHtml(gstNumber)

        val subtotal = order.items.sumOf { it.totalPrice }
        val computedTotal = order.totalAmount
        
        val itemsHtml = order.items.joinToString("") { item ->
            """
            <tr>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;">
                  ${escapeHtml(item.menuItemName)}
                  ${if (!item.specialInstructions.isNullOrBlank()) "<br><small style=\"color:#666\">+ ${escapeHtml(item.specialInstructions)}</small>" else ""}
              </td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:center;">${item.quantity}</td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:right;">&#8377;${String.format(Locale.US, "%.2f", item.unitPrice)}</td>
              <td style="padding:8px 0;border-bottom:1px dashed #ccc;text-align:right;">&#8377;${String.format(Locale.US, "%.2f", item.totalPrice)}</td>
            </tr>
            """.trimIndent()
        }

        val dateString = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = sdf.parse(order.updatedAt) ?: Date()
            SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(date)
        } catch (e: Exception) {
            order.updatedAt
        }

        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8">
          <title>Invoice — $safeShopName</title>
          <style>
            body{font-family:'Courier New', Courier, monospace;max-width:350px;margin:0 auto;padding:20px;color:#000;font-size:14px;line-height:1.4}
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
          <h2>$safeShopName</h2>
          <div class="info">
            ${if (safeAddress.isNotBlank()) "$safeAddress<br>" else ""}
            ${if (safePhone.isNotBlank()) "Ph: $safePhone<br>" else ""}
            ${if (safeEmail.isNotBlank()) "$safeEmail<br>" else ""}
            ${if (safeGst.isNotBlank()) "GSTIN: $safeGst" else ""}
          </div>
          
          <div class="divider"></div>
          
          <div class="order-meta">
            <div><strong>Order #:</strong> ${escapeHtml(order.queueNumber)}</div>
            <div><strong>Date:</strong> $dateString</div>
            <div><strong>Payment:</strong> UNPAID (Pay at Counter)</div>
          </div>
          
          <div class="divider"></div>
          
          <table>
            <thead><tr>
              <th>Item</th><th style="text-align:center">Qty</th>
              <th style="text-align:right">Price</th><th style="text-align:right">Total</th>
            </tr></thead>
            <tbody>$itemsHtml</tbody>
          </table>
          
          <table class="totals-table">
            <tr>
                <td>Subtotal</td>
                <td style="text-align:right">&#8377;${String.format(Locale.US, "%.2f", subtotal)}</td>
            </tr>
            <tr>
                <td class="grand-total" style="padding-top:10px">Total</td>
                <td class="grand-total" style="text-align:right;padding-top:10px">&#8377;${String.format(Locale.US, "%.2f", computedTotal)}</td>
            </tr>
          </table>
          
          ${if (!order.customerNote.isNullOrBlank()) "<div class=\"instruction\"><strong>Instruction:</strong> ${escapeHtml(order.customerNote)}</div>" else ""}
          
          <div class="divider"></div>
          
          <div class="footer">
            Thank you for visiting!<br>
            Powered by Scan2Skip
          </div>
          <script>window.onload=()=>window.print()</script>
        </body></html>
        """.trimIndent()
    }
}
