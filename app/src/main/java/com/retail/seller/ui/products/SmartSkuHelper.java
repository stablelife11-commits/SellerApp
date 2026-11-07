package com.retail.seller.ui.products;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.Locale;

public class SmartSkuHelper {

    /**
     * 2 words ("Lux Cozi") -> "LUC" (2 chars from 1st + 1 char from 2nd)
     * 1 word  ("Rupa")     -> "RUP" (first 3 chars)
     */
    public static String smartCode(String input, String fallback) {
        if (input == null || input.trim().isEmpty()) {
            return fallback;
        }
        String[] words = input.trim().toUpperCase(Locale.ROOT).split("\\s+");

        if (words.length >= 2) {
            String w1 = words[0].replaceAll("[^A-Z0-9]", "");
            String w2 = words[1].replaceAll("[^A-Z0-9]", "");
            if (w1.length() >= 2 && w2.length() >= 1) {
                return w1.substring(0, 2) + w2.substring(0, 1);
            }
        }

        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            return fallback;
        }
        return cleaned.length() >= 3 ? cleaned.substring(0, 3) : cleaned;
    }

    /**
     * Normalizes garment sizes: "85cm", "85 CM", "85-cm" -> "85"
     */
    public static String normalizeSize(String size) {
        if (size == null || size.trim().isEmpty()) {
            return "FS";
        }
        String s = size.toUpperCase(Locale.ROOT).trim()
                .replaceAll("CM|CMS|INCH|INCHES|\\s+", "")
                .replaceAll("[^A-Z0-9]", "");
        return s.isEmpty() ? "FS" : s;
    }

    /**
     * Generates Product Style Code: e.g., "LUC-MEV-VEN"
     */
    public static String generateProductCode(String brand, String category, String productName) {
        String b = smartCode(brand, "GEN");
        String c = smartCode(category, "APP");
        String p = smartCode(productName, "ITM");
        return String.format(Locale.ROOT, "%s-%s-%s", b, c, p);
    }

    /**
     * Generates Variant SKU: e.g., "LUC-MEV-VEN-WHI-85"
     */
    public static String generateVariantSku(String brand, String category, String productName, String color, String size) {
        String b = smartCode(brand, "GEN");
        String c = smartCode(category, "APP");
        String p = smartCode(productName, "ITM");
        String col = smartCode(color, "STD");
        String s = normalizeSize(size);
        return String.format(Locale.ROOT, "%s-%s-%s-%s-%s", b, c, p, col, s);
    }

    /**
     * Binds live auto-generation to AddProductActivity EditTexts
     */
    public static void bindLiveSkuGenerator(
            EditText etBrand,
            EditText etCategory,
            EditText etProductName,
            EditText etColor,
            EditText etSize,
            EditText etProductCode,
            EditText etSku,
            boolean isEditMode) {

        final boolean[] isManualCode = {isEditMode};
        final boolean[] isManualSku = {isEditMode};
        final boolean[] isProgrammaticChange = {false};

        if (etProductCode != null) {
            etProductCode.addTextChangedListener(new SimpleWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (!isProgrammaticChange[0] && etProductCode.hasFocus()) {
                        isManualCode[0] = !s.toString().trim().isEmpty();
                    }
                }
            });
        }

        if (etSku != null) {
            etSku.addTextChangedListener(new SimpleWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (!isProgrammaticChange[0] && etSku.hasFocus()) {
                        isManualSku[0] = !s.toString().trim().isEmpty();
                    }
                }
            });
        }

        TextWatcher autoSyncWatcher = new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                isProgrammaticChange[0] = true;

                String brand = getText(etBrand);
                String category = getText(etCategory);
                String name = getText(etProductName);
                String color = getText(etColor);
                String size = getText(etSize);

                if (etProductCode != null && !isManualCode[0]) {
                    etProductCode.setText(generateProductCode(brand, category, name));
                }

                if (etSku != null && !isManualSku[0]) {
                    etSku.setText(generateVariantSku(brand, category, name, color, size));
                }

                isProgrammaticChange[0] = false;
            }
        };

        if (etBrand != null) etBrand.addTextChangedListener(autoSyncWatcher);
        if (etCategory != null) etCategory.addTextChangedListener(autoSyncWatcher);
        if (etProductName != null) etProductName.addTextChangedListener(autoSyncWatcher);
        if (etColor != null) etColor.addTextChangedListener(autoSyncWatcher);
        if (etSize != null) etSize.addTextChangedListener(autoSyncWatcher);
    }

    private static String getText(EditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    public abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}