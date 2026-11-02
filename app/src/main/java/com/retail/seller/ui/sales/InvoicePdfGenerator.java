package com.retail.seller.ui.sales;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.network.TokenManager;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public class InvoicePdfGenerator {

    public static void generateAndShareInvoice(Context context, SaleResponseDto order) {
        if (order == null || order.getId() == null) {
            Toast.makeText(context, "Order data unavailable for invoice.", Toast.LENGTH_SHORT).show();
            return;
        }

        TokenManager tm = TokenManager.getInstance(context);
        String sellerName = order.getSellerBusinessName() != null && !order.getSellerBusinessName().isEmpty()
                ? order.getSellerBusinessName() : tm.getBusinessName();
        String sellerGst = order.getSellerGstNumber() != null && !order.getSellerGstNumber().isEmpty()
                ? order.getSellerGstNumber() : tm.getGstNumber();
        String sellerPhone = order.getSellerPhone() != null && !order.getSellerPhone().isEmpty()
                ? order.getSellerPhone() : tm.getBusinessPhone();
        String sellerAddr = order.getSellerAddress() != null && !order.getSellerAddress().isEmpty()
                ? order.getSellerAddress() : tm.getBusinessAddress();

        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 595;  // A4 width in points
        int pageHeight = 842; // A4 height in points

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(16f);
        titlePaint.setColor(Color.parseColor("#0F172A"));

        Paint subHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subHeaderPaint.setTextSize(11f);
        subHeaderPaint.setColor(Color.parseColor("#1E3A8A"));

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        bodyPaint.setTextSize(10f);
        bodyPaint.setColor(Color.parseColor("#334155"));

        Paint boldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        boldPaint.setTextSize(10f);
        boldPaint.setColor(Color.parseColor("#0F172A"));

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#CBD5E1"));
        linePaint.setStrokeWidth(1f);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.parseColor("#EFF6FF"));

        int leftMargin = 36;
        int rightMargin = pageWidth - 36;
        int y = 45;

        // 1. Top Title Banner
        canvas.drawText("TAX INVOICE (B2B WHOLESALE)", leftMargin, y, titlePaint);
        String invNo = "INV-" + order.getId();
        float invWidth = boldPaint.measureText("Invoice No: " + invNo);
        canvas.drawText("Invoice No: " + invNo, rightMargin - invWidth, y, boldPaint);
        y += 16;

        String dateStr = order.getSaleDate() != null ? order.getSaleDate().replace("T", " ") : "N/A";
        if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
        canvas.drawText("Order Status: " + order.getStatus() + "  |  Payment: " + order.getPaymentStatus(), leftMargin, y, bodyPaint);
        float dateWidth = bodyPaint.measureText("Date: " + dateStr);
        canvas.drawText("Date: " + dateStr, rightMargin - dateWidth, y, bodyPaint);
        y += 14;

        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
        y += 18;

        // 2. Seller & Buyer Two-Column Block
        int midX = pageWidth / 2 + 10;
        int startYBlock = y;

        canvas.drawText("SOLD BY (WHOLESALE SELLER):", leftMargin, y, subHeaderPaint);
        y += 14;
        canvas.drawText(sellerName, leftMargin, y, boldPaint);
        y += 13;
        canvas.drawText("GSTIN: " + sellerGst, leftMargin, y, boldPaint);
        y += 13;
        if (!sellerPhone.isEmpty()) {
            canvas.drawText("Phone: " + sellerPhone, leftMargin, y, bodyPaint);
            y += 13;
        }
        canvas.drawText(truncate(sellerAddr, 44), leftMargin, y, bodyPaint);

        int buyerY = startYBlock;
        canvas.drawText("BILLED & SHIPPED TO (BUYER):", midX, buyerY, subHeaderPaint);
        buyerY += 14;
        canvas.drawText(order.getCustomerName() != null ? order.getCustomerName() : "Wholesale Buyer", midX, buyerY, boldPaint);
        buyerY += 13;
        canvas.drawText("Mobile: " + (order.getCustomerMobile() != null ? order.getCustomerMobile() : "N/A"), midX, buyerY, bodyPaint);
        buyerY += 13;
        String addr = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Store Delivery";
        canvas.drawText("Address: " + truncate(addr, 40), midX, buyerY, bodyPaint);

        y = Math.max(y, buyerY) + 20;
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
        y += 6;

        // 3. Line Items Table Header
        canvas.drawRect(leftMargin, y, rightMargin, y + 22, headerBgPaint);
        int textY = y + 15;
        canvas.drawText("#", leftMargin + 6, textY, boldPaint);
        canvas.drawText("Item / Size / Color", leftMargin + 24, textY, boldPaint);
        canvas.drawText("HSN", leftMargin + 230, textY, boldPaint);
        canvas.drawText("Qty", leftMargin + 280, textY, boldPaint);
        canvas.drawText("Rate (₹)", leftMargin + 320, textY, boldPaint);
        canvas.drawText("GST", leftMargin + 385, textY, boldPaint);
        canvas.drawText("Total (₹)", leftMargin + 455, textY, boldPaint);
        y += 22;
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
        y += 16;

        // 4. Line Items Rows
        List<SaleItemDto> items = order.getItems();
        int totalUnits = 0;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if (y > pageHeight - 190) break;
                SaleItemDto item = items.get(i);
                int qty = item.getQuantity();
                totalUnits += qty;
                double price = item.getPrice();
                double lineTotal = qty * price;
                String sizeColor = ((item.getSize() != null ? item.getSize() : "") + " "
                        + (item.getColor() != null ? item.getColor() : "")).trim();
                String itemDesc = truncate(item.getProductName() + (sizeColor.isEmpty() ? "" : " (" + sizeColor + ")"), 34);
                String hsn = item.getHsnCode() != null && !item.getHsnCode().isEmpty() ? item.getHsnCode() : "-";
                String gstStr = String.format(Locale.US, "%.0f%%", item.getGstPercentage());

                canvas.drawText(String.valueOf(i + 1), leftMargin + 6, y, bodyPaint);
                canvas.drawText(itemDesc, leftMargin + 24, y, bodyPaint);
                canvas.drawText(hsn, leftMargin + 230, y, bodyPaint);
                canvas.drawText(String.valueOf(qty), leftMargin + 280, y, bodyPaint);
                canvas.drawText(String.format(Locale.US, "%.2f", price), leftMargin + 320, y, bodyPaint);
                canvas.drawText(gstStr, leftMargin + 385, y, bodyPaint);
                canvas.drawText(String.format(Locale.US, "%.2f", lineTotal), leftMargin + 455, y, boldPaint);
                y += 18;
            }
        }

        y += 4;
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint);
        y += 18;

        // 5. Totals, GST Breakdown (CGST + SGST), Return Credit & Net Balance Summary
        double totalTax = order.getTaxAmount();
        double halfTax = totalTax / 2.0;
        double taxableValue = Math.max(0.0, order.getTotalAmount() - totalTax);

        canvas.drawText("Total Qty: " + totalUnits + " pcs", leftMargin, y, boldPaint);
        drawRightAlignedRow(canvas, "Gross Subtotal:", String.format(Locale.US, "₹%.2f", order.getSubtotalAmount()), 360, rightMargin, y, bodyPaint, boldPaint);
        y += 15;

        if (order.getDiscountAmount() > 0) {
            drawRightAlignedRow(canvas, "Seller Discount:", String.format(Locale.US, "-₹%.2f", order.getDiscountAmount()), 360, rightMargin, y, bodyPaint, boldPaint);
            y += 15;
        }

        drawRightAlignedRow(canvas, "Taxable Value (Excl. GST):", String.format(Locale.US, "₹%.2f", taxableValue), 360, rightMargin, y, bodyPaint, bodyPaint);
        y += 15;
        drawRightAlignedRow(canvas, "CGST (50% of GST):", String.format(Locale.US, "₹%.2f", halfTax), 360, rightMargin, y, bodyPaint, bodyPaint);
        y += 15;
        drawRightAlignedRow(canvas, "SGST (50% of GST):", String.format(Locale.US, "₹%.2f", halfTax), 360, rightMargin, y, bodyPaint, bodyPaint);
        y += 15;
        drawRightAlignedRow(canvas, "Invoice Total (Incl. GST):", String.format(Locale.US, "₹%.2f", order.getTotalAmount()), 360, rightMargin, y, boldPaint, boldPaint);
        y += 15;

        if (order.getReturnedAmount() > 0) {
            drawRightAlignedRow(canvas, "Less: Returned Goods Credit:", String.format(Locale.US, "-₹%.2f", order.getReturnedAmount()), 360, rightMargin, y, bodyPaint, boldPaint);
            y += 15;
        }

        canvas.drawLine(350, y - 6, rightMargin, y - 6, linePaint);
        y += 6;
        drawRightAlignedRow(canvas, "NET PAYABLE AMOUNT:", String.format(Locale.US, "₹%.2f", order.getNetPayableAmount()), 350, rightMargin, y, subHeaderPaint, subHeaderPaint);
        y += 16;

        drawRightAlignedRow(canvas, "Amount Received (" + order.getPaymentStatus() + "):", String.format(Locale.US, "₹%.2f", order.getPaidAmount()), 350, rightMargin, y, bodyPaint, boldPaint);
        y += 15;

        if (order.getRefundCreditAmount() > 0.009) {
            drawRightAlignedRow(canvas, "Refund / Credit Due to Buyer:", String.format(Locale.US, "₹%.2f", order.getRefundCreditAmount()), 350, rightMargin, y, boldPaint, boldPaint);
        } else {
            drawRightAlignedRow(canvas, "Balance Due:", String.format(Locale.US, "₹%.2f", order.getBalanceDueAmount()), 350, rightMargin, y, boldPaint, boldPaint);
        }
        y += 28;

        if (order.getSellerRemarks() != null && !order.getSellerRemarks().trim().isEmpty()) {
            canvas.drawText("Seller Note: " + truncate(order.getSellerRemarks().trim(), 75), leftMargin, y, bodyPaint);
            y += 18;
        }

        // Footer
        canvas.drawLine(leftMargin, pageHeight - 60, rightMargin, pageHeight - 60, linePaint);
        canvas.drawText("Computer generated B2B GST Tax Invoice • Retail Flow Wholesale ERP", leftMargin, pageHeight - 42, bodyPaint);
        canvas.drawText("Authorized Signatory", rightMargin - 115, pageHeight - 42, boldPaint);

        pdfDocument.finishPage(page);

        try {
            File invoicesDir = new File(context.getCacheDir(), "invoices");
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs();
            }
            File pdfFile = new File(invoicesDir, "GST_Invoice_Order_" + order.getId() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                pdfDocument.writeTo(fos);
            }
            pdfDocument.close();

            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GST Tax Invoice - Order #" + order.getId());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the B2B GST Tax Invoice for Order #" + order.getId()
                    + " (Net Payable: ₹" + String.format(Locale.US, "%.2f", order.getNetPayableAmount()) + ").");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share or Print GST Invoice PDF"));
        } catch (Exception e) {
            pdfDocument.close();
            Toast.makeText(context, "Failed to generate PDF: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void drawRightAlignedRow(Canvas canvas, String label, String value, int labelX, int rightEdge, int y, Paint labelPaint, Paint valPaint) {
        canvas.drawText(label, labelX, y, labelPaint);
        float w = valPaint.measureText(value);
        canvas.drawText(value, rightEdge - w, y, valPaint);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "…";
    }
}