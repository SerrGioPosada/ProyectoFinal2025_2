package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.LineItem;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class with static methods to handle the creation of PDF documents.
 */
public class PdfUtility {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGIN = 50;
    private static final float LEADING = 15;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_SUBTITLE = 14;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 8;

    /**
     * Generates a PDF invoice for the given invoice and user.
     *
     * @param invoice The invoice to generate PDF for
     * @param user    The user associated with the invoice
     * @return The generated PDF file
     * @throws IOException If an error occurs during PDF generation
     */
    public static File generateInvoicePDF(Invoice invoice, User user) throws IOException {
        // Create directory if it doesn't exist
        File invoicesDir = new File("invoices");
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs();
        }

        // Create PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Header - Company name
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("SISTEMA DE ENVIOS UQ");
            contentStream.endText();
            yPosition -= LEADING * 2;

            // Invoice title
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("FACTURA");
            contentStream.endText();
            yPosition -= LEADING * 2;

            // Invoice details
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

            // Invoice number and date
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Factura No: " + invoice.getInvoiceNumber());
            contentStream.endText();
            yPosition -= LEADING;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Fecha: " + invoice.getIssuedAt().format(DATETIME_FORMATTER));
            contentStream.endText();
            yPosition -= LEADING * 2;

            // Customer information
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("DATOS DEL CLIENTE");
            contentStream.endText();
            yPosition -= LEADING * 1.5f;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Nombre: " + user.getName() + " " + user.getLastName());
            contentStream.endText();
            yPosition -= LEADING;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Email: " + user.getEmail());
            contentStream.endText();
            yPosition -= LEADING;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("ID Usuario: " + user.getId());
            contentStream.endText();
            yPosition -= LEADING * 2;

            // Line items table header
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("DETALLE DE LA FACTURA");
            contentStream.endText();
            yPosition -= LEADING * 1.5f;

            // Draw horizontal line
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            yPosition -= LEADING;

            // Table headers
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Descripción");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(450, yPosition);
            contentStream.showText("Monto");
            contentStream.endText();
            yPosition -= LEADING;

            // Draw horizontal line
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            yPosition -= LEADING;

            // Line items
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            for (LineItem item : invoice.getLineItems()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(item.getDescription());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(450, yPosition);
                contentStream.showText(String.format("$%.2f", item.getAmount()));
                contentStream.endText();
                yPosition -= LEADING;
            }

            yPosition -= LEADING;

            // Draw horizontal line
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            yPosition -= LEADING * 1.5f;

            // Total
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(350, yPosition);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(450, yPosition);
            contentStream.showText(String.format("$%.2f", invoice.getTotalAmount()));
            contentStream.endText();
            yPosition -= LEADING * 3;

            // Footer
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Gracias por utilizar nuestros servicios.");
            contentStream.endText();
            yPosition -= LEADING;

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Para cualquier consulta, contáctenos a través de nuestro correo electrónico.");
            contentStream.endText();
        }

        // Save PDF
        String fileName = "invoices/Factura_" + invoice.getInvoiceNumber() + ".pdf";
        File pdfFile = new File(fileName);
        document.save(pdfFile);
        document.close();

        return pdfFile;
    }

    /**
     * Creates a professional PDF report with header, title, and table data.
     *
     * @param fileName    The file name (without extension)
     * @param title       Report title
     * @param subtitle    Report subtitle (e.g., date range)
     * @param headers     Table column headers
     * @param rows        Table data rows
     * @return File object pointing to the generated PDF
     */
    public static File generatePdfReport(String fileName, String title, String subtitle,
                                          List<String> headers, List<List<String>> rows) throws IOException {
        // Ensure directory exists
        File directory = new File("reportes");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fullPath = "reportes/" + fileName + ".pdf";
        File file = new File(fullPath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Draw header
            yPosition = drawHeader(contentStream, page, yPosition, title, subtitle);

            // Calculate column widths
            float tableWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
            float[] columnWidths = calculateColumnWidths(headers.size(), tableWidth);

            // Draw table headers
            yPosition = drawTableHeaders(contentStream, headers, columnWidths, yPosition, MARGIN);

            // Draw table rows
            int rowCount = 0;
            for (List<String> row : rows) {
                // Check if we need a new page
                if (yPosition < MARGIN + 50) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - MARGIN;
                    yPosition = drawTableHeaders(contentStream, headers, columnWidths, yPosition, MARGIN);
                }

                yPosition = drawTableRow(contentStream, row, columnWidths, yPosition, MARGIN, rowCount % 2 == 0);
                rowCount++;
            }

            // Draw footer
            drawFooter(contentStream, page);

            contentStream.close();
            document.save(file);

            Logger.info("PDF report generated successfully: " + fullPath);
            return file;

        } catch (IOException e) {
            Logger.error("Error generating PDF report", e);
            throw e;
        }
    }

    /**
     * Generates a statistics/summary PDF report (for general reports).
     */
    public static File generateStatisticsPdfReport(String fileName, String title, String subtitle,
                                                     List<StatisticItem> statistics) throws IOException {
        File directory = new File("reportes");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fullPath = "reportes/" + fileName + ".pdf";
        File file = new File(fullPath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Draw header
            yPosition = drawHeader(contentStream, page, yPosition, title, subtitle);

            // Draw statistics cards
            yPosition -= 30;
            for (StatisticItem stat : statistics) {
                yPosition = drawStatisticCard(contentStream, stat, yPosition, MARGIN,
                                               page.getMediaBox().getWidth() - (2 * MARGIN));
                yPosition -= 20; // Space between cards
            }

            // Draw footer
            drawFooter(contentStream, page);

            contentStream.close();
            document.save(file);

            Logger.info("Statistics PDF report generated successfully: " + fullPath);
            return file;

        } catch (IOException e) {
            Logger.error("Error generating statistics PDF report", e);
            throw e;
        }
    }

    private static float drawHeader(PDPageContentStream contentStream, PDPage page, float yPosition,
                                     String title, String subtitle) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();

        // Draw title
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                .getStringWidth(title) / 1000 * FONT_SIZE_TITLE;
        contentStream.beginText();
        contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= 25;

        // Draw subtitle
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SUBTITLE);
        float subtitleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                .getStringWidth(subtitle) / 1000 * FONT_SIZE_SUBTITLE;
        contentStream.beginText();
        contentStream.newLineAtOffset((pageWidth - subtitleWidth) / 2, yPosition);
        contentStream.showText(subtitle);
        contentStream.endText();
        yPosition -= 20;

        // Draw generation date
        String generatedText = "Generado: " + LocalDateTime.now().format(DATETIME_FORMATTER);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
        float dateWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                .getStringWidth(generatedText) / 1000 * FONT_SIZE_SMALL;
        contentStream.beginText();
        contentStream.newLineAtOffset((pageWidth - dateWidth) / 2, yPosition);
        contentStream.showText(generatedText);
        contentStream.endText();
        yPosition -= 30;

        // Draw separator line
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();
        yPosition -= 20;

        return yPosition;
    }

    private static float[] calculateColumnWidths(int numColumns, float totalWidth) {
        float[] widths = new float[numColumns];
        float columnWidth = totalWidth / numColumns;
        for (int i = 0; i < numColumns; i++) {
            widths[i] = columnWidth;
        }
        return widths;
    }

    private static float drawTableHeaders(PDPageContentStream contentStream, List<String> headers,
                                           float[] columnWidths, float yPosition, float xStart) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_NORMAL);

        // Draw header background
        float tableWidth = 0;
        for (float width : columnWidths) {
            tableWidth += width;
        }
        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        contentStream.addRect(xStart, yPosition - LEADING, tableWidth, LEADING + 5);
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f);

        // Draw header text
        float xPosition = xStart + 5;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition - 10);

        for (int i = 0; i < headers.size(); i++) {
            String header = truncateText(headers.get(i), columnWidths[i] - 10, FONT_SIZE_NORMAL);
            contentStream.showText(header);
            if (i < headers.size() - 1) {
                contentStream.newLineAtOffset(columnWidths[i], 0);
            }
        }
        contentStream.endText();

        yPosition -= LEADING + 5;
        return yPosition;
    }

    private static float drawTableRow(PDPageContentStream contentStream, List<String> row,
                                       float[] columnWidths, float yPosition, float xStart,
                                       boolean alternateColor) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);

        // Draw row background (alternate colors)
        if (alternateColor) {
            float tableWidth = 0;
            for (float width : columnWidths) {
                tableWidth += width;
            }
            contentStream.setNonStrokingColor(0.97f, 0.97f, 0.97f);
            contentStream.addRect(xStart, yPosition - LEADING, tableWidth, LEADING);
            contentStream.fill();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
        }

        // Draw row text
        float xPosition = xStart + 5;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition - 10);

        for (int i = 0; i < row.size() && i < columnWidths.length; i++) {
            String cell = truncateText(row.get(i), columnWidths[i] - 10, FONT_SIZE_NORMAL);
            contentStream.showText(cell);
            if (i < row.size() - 1) {
                contentStream.newLineAtOffset(columnWidths[i], 0);
            }
        }
        contentStream.endText();

        yPosition -= LEADING;
        return yPosition;
    }

    private static float drawStatisticCard(PDPageContentStream contentStream, StatisticItem stat,
                                            float yPosition, float xStart, float cardWidth) throws IOException {
        float cardHeight = 60;

        // Draw card background
        contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f);
        contentStream.addRect(xStart, yPosition - cardHeight, cardWidth, cardHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f);

        // Draw card border
        contentStream.setLineWidth(1f);
        contentStream.addRect(xStart, yPosition - cardHeight, cardWidth, cardHeight);
        contentStream.stroke();

        // Draw label
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        contentStream.beginText();
        contentStream.newLineAtOffset(xStart + 15, yPosition - 20);
        contentStream.showText(stat.label);
        contentStream.endText();

        // Draw value
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        contentStream.beginText();
        contentStream.newLineAtOffset(xStart + 15, yPosition - 45);
        contentStream.showText(stat.value);
        contentStream.endText();

        return yPosition - cardHeight;
    }

    private static void drawFooter(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float footerY = MARGIN - 20;

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
        String footerText = "Sistema de Gestion de Envios - Reporte generado automaticamente";
        float footerWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                .getStringWidth(footerText) / 1000 * FONT_SIZE_SMALL;

        contentStream.beginText();
        contentStream.newLineAtOffset((pageWidth - footerWidth) / 2, footerY);
        contentStream.showText(footerText);
        contentStream.endText();
    }

    private static String truncateText(String text, float maxWidth, float fontSize) {
        try {
            float textWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                    .getStringWidth(text) / 1000 * fontSize;

            if (textWidth <= maxWidth) {
                return text;
            }

            // Truncate and add ellipsis
            while (textWidth > maxWidth && text.length() > 3) {
                text = text.substring(0, text.length() - 1);
                textWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
                        .getStringWidth(text + "...") / 1000 * fontSize;
            }
            return text + "...";
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Helper class to represent a statistic item for summary reports.
     */
    public static class StatisticItem {
        public final String label;
        public final String value;

        public StatisticItem(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}
