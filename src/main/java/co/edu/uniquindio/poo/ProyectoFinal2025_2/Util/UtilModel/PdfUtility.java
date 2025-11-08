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
import java.time.format.DateTimeFormatter;

/**
 * A utility class with static methods to handle the creation of PDF documents.
 */
public class PdfUtility {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGIN = 50;
    private static final float LEADING = 15;

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
}
