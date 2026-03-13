package com.ai.platform.api.finance;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfTransactionParser {

    private static final Pattern DATE =
            Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{2}|\\d{2}/\\d{2})\\b");

    private static final Pattern AMOUNT =
            Pattern.compile("-?[\\d,]+\\.\\d{2}");
	
	private static final Pattern DATE_LINE =
        Pattern.compile("^\\d{2}/\\d{2}/\\d{2}");
		
    public List<Txn> parsePdf(List<MultipartFile> files, String monthKey) {

        List<Txn> txns = new ArrayList<>();

        for (MultipartFile file : files) {

            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf"))
                continue;

            try (InputStream in = file.getInputStream();
                 PDDocument doc = PDDocument.load(in)) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc);

                boolean isCreditCard =
					text.toLowerCase().contains("payment due date") ||
					text.toLowerCase().contains("purchases and adjustments") ||
					text.toLowerCase().contains("payments and other credits");

                String[] lines = text.split("\\r?\\n");

                int baseYear = Integer.parseInt(monthKey.substring(0, 4));
                int targetMonth = Integer.parseInt(monthKey.substring(5, 7));

                LocalDate currentDate = null;
StringBuilder descBuilder = new StringBuilder();

for (String line : lines) {

    String clean = line.trim();
    if (clean.isEmpty()) continue;
	
	if (clean.toLowerCase().startsWith("total")) continue;

    Matcher dateMatcher = DATE_LINE.matcher(clean);
    Matcher amtMatcher = AMOUNT.matcher(clean);

// ---------- Start of transaction ----------
if (dateMatcher.find()) {

    String dateStr = dateMatcher.group();
    String[] parts = dateStr.split("/");

    int month = Integer.parseInt(parts[0]);
    int day = Integer.parseInt(parts[1]);

    int year = baseYear;
    if (month > targetMonth) year = baseYear - 1;

    LocalDate date = LocalDate.of(year, month, day);

    // check if amount exists on same line
    if (amtMatcher.find()) {

        String amtStr = amtMatcher.group().replace(",", "");
        BigDecimal amount = new BigDecimal(amtStr);

        if (isCreditCard) {
            amount = amount.negate();
        }

        String desc = clean.substring(dateMatcher.end(), amtMatcher.start()).trim();

        txns.add(new Txn(date, desc, amount));
        currentDate = null;
        descBuilder.setLength(0);

    } else {

        // multiline transaction
        currentDate = date;

        descBuilder.setLength(0);

        String afterDate = clean.substring(dateMatcher.end()).trim();
        if (!afterDate.isEmpty()) {
            descBuilder.append(afterDate);
        }
    }

    continue;
}

    // ---------- Amount line (end transaction) ----------
    if (amtMatcher.find() && currentDate != null) {

        String amtStr = amtMatcher.group().replace(",", "");
        BigDecimal amount = new BigDecimal(amtStr);

        if (isCreditCard) {
            amount = amount.negate();
        }

        String desc = descBuilder.toString().trim();

        txns.add(new Txn(currentDate, desc, amount));

        currentDate = null;
        descBuilder.setLength(0);

        continue;
    }

    // ---------- Description continuation ----------
    if (currentDate != null) {
        descBuilder.append(" ").append(clean);
    }
}

            } catch (Exception e) {
                throw new RuntimeException("Failed to parse PDF " + file.getOriginalFilename(), e);
            }
        }

        txns.sort(Comparator.comparing(Txn::date)
                .thenComparing(Txn::description));

        return txns;
    }
}