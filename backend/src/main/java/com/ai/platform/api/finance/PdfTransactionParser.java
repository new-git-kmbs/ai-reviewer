package com.ai.platform.api.finance;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

@Service
public class PdfTransactionParser {

    private static final Pattern ROW =
            Pattern.compile("(\\d{2}/\\d{2})\\s+(\\d{2}/\\d{2})\\s+(.*?)\\s+(-?\\d+\\.\\d{2})$");

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public List<Txn> parsePdf(List<MultipartFile> files, String monthKey){

        List<Txn> txns = new ArrayList<>();

        for (MultipartFile file : files) {

            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                continue;
            }

            try (InputStream in = file.getInputStream();
                 PDDocument doc = PDDocument.load(in)) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc);
				boolean isCreditCard = text.toLowerCase().contains("payment due date");

                String[] lines = text.split("\\r?\\n");

                int baseYear = Integer.parseInt(monthKey.substring(0, 4));

                for (String line : lines) {

                    Matcher m = ROW.matcher(line.trim());
                    if (!m.find()) continue;

                    String txnDate = m.group(1);
                    String desc = m.group(3).trim();
                    String amt = m.group(4);

                    String[] parts = txnDate.split("/");
					int month = Integer.parseInt(parts[0]);
					int day = Integer.parseInt(parts[1]);

					int targetMonth = Integer.parseInt(monthKey.substring(5, 7));

					int year = baseYear;

					/* Months after the target month belong to previous year */
					if (month > targetMonth) {
						year = baseYear - 1;
					}

					LocalDate date = LocalDate.of(year, month, day);

                    BigDecimal amount = new BigDecimal(amt);

					/* Credit card statements list purchases as positive numbers.
					Convert them to negative so they behave like expenses. */
					if (isCreditCard && amount.compareTo(BigDecimal.ZERO) > 0) {
						amount = amount.negate();
					}

                    txns.add(new Txn(date, desc, amount));
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to parse PDF " + file.getOriginalFilename(), e);
            }
        }
		txns.sort(Comparator.comparing(Txn::date).thenComparing(Txn::description));

		return txns;


    }
}