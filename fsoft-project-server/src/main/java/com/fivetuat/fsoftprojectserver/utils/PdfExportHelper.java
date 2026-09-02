package com.fivetuat.fsoftprojectserver.utils;

import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import java.util.List;

public class PdfExportHelper {

    private PdfExportHelper() {
    }

    public static byte[] exportCardsToPdf(Deck deck, List<Card> cards) {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font configuration
            Font titleFont;
            Font headerFont;
            Font dataFont;

            try {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                titleFont = new Font(bf, 16, Font.BOLD, new Color(15, 23, 42));
                headerFont = new Font(bf, 10, Font.BOLD, Color.WHITE);
                dataFont = new Font(bf, 9, Font.NORMAL, new Color(51, 65, 85));
            } catch (Exception e) {
                titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(15, 23, 42));
                headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
                dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(51, 65, 85));
            }

            // Title
            String titleText = "BỘ BÀI: " + (deck != null && deck.getTitle() != null ? deck.getTitle() : "FLASHCARDS");
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle
            Paragraph subtitle = new Paragraph("TỔNG SỐ THẺ: " + cards.size(), dataFont);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Table with 7 columns
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 5f, 4f, 3f, 6f, 7f, 6f});

            // Table Headers
            String[] headers = {"STT", "Word", "Phonetic", "Type", "Meaning", "Example", "Example Meaning"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Data
            int stt = 1;
            for (Card card : cards) {
                addCell(table, String.valueOf(stt++), dataFont, Element.ALIGN_CENTER);
                addCell(table, card.getWord(), dataFont, Element.ALIGN_LEFT);
                addCell(table, card.getPhonetic(), dataFont, Element.ALIGN_LEFT);
                addCell(table, card.getPartOfSpeech(), dataFont, Element.ALIGN_CENTER);
                addCell(table, card.getMeaning(), dataFont, Element.ALIGN_LEFT);
                addCell(table, card.getExampleSentence(), dataFont, Element.ALIGN_LEFT);
                addCell(table, card.getExampleMeaning(), dataFont, Element.ALIGN_LEFT);
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Lỗi khi tạo file PDF: " + e.getMessage(), e);
        }
    }

    private static void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
