package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;

/**
 * Service for sending emails with attachments.
 * Handles invoice delivery and notifications.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "sistema.envios.uq@gmail.com"; // Replace with actual email
    private static final String FROM_PASSWORD = "your_app_password_here"; // Replace with actual app password

    private static EmailService instance;

    private EmailService() {}

    /**
     * Gets the singleton instance of EmailService.
     */
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Sends an email with a PDF attachment.
     *
     * @param toEmail    Recipient email address
     * @param subject    Email subject
     * @param body       Email body content
     * @param attachment PDF file to attach (optional)
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendEmailWithAttachment(String toEmail, String subject, String body, File attachment) {
        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Create authenticator
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                }
            };

            // Create session
            Session session = Session.getInstance(props, auth);

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Create multipart message
            Multipart multipart = new MimeMultipart();

            // Add text body part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(body);
            multipart.addBodyPart(textBodyPart);

            // Add attachment if provided
            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachmentBodyPart);
            }

            // Set content
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            Logger.info("Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException e) {
            Logger.error("Error sending email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends an invoice email with PDF attachment.
     *
     * @param toEmail       Recipient email address
     * @param invoiceNumber Invoice number
     * @param pdfFile       Invoice PDF file
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendInvoiceEmail(String toEmail, String invoiceNumber, File pdfFile) {
        String subject = "Factura " + invoiceNumber + " - Sistema de Envíos UQ";
        String body = "Estimado cliente,\n\n" +
                "Adjunto encontrará la factura " + invoiceNumber + " correspondiente a su pedido.\n\n" +
                "Gracias por utilizar nuestros servicios.\n\n" +
                "Saludos,\n" +
                "Sistema de Envíos UQ";

        return sendEmailWithAttachment(toEmail, subject, body, pdfFile);
    }

    /**
     * Sends a notification email to a delivery person about a new shipment assignment.
     *
     * @param toEmail           Delivery person's email
     * @param deliveryPersonName Delivery person's name
     * @param shipmentId        Shipment ID
     * @param origin            Origin address
     * @param destination       Destination address
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendShipmentAssignmentEmail(String toEmail, String deliveryPersonName, String shipmentId,
                                               String origin, String destination) {
        String subject = "Nuevo Envío Asignado - " + shipmentId;
        String body = "Hola " + deliveryPersonName + ",\n\n" +
                "Se te ha asignado un nuevo envío:\n\n" +
                "ID del Envío: " + shipmentId + "\n" +
                "Origen: " + origin + "\n" +
                "Destino: " + destination + "\n\n" +
                "Por favor, ingresa al sistema para ver los detalles completos del envío.\n\n" +
                "Saludos,\n" +
                "Sistema de Envíos UQ";

        return sendEmailWithAttachment(toEmail, subject, body, null);
    }

    /**
     * Sends a simple notification email without attachment.
     *
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body    Email body content
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendNotificationEmail(String toEmail, String subject, String body) {
        return sendEmailWithAttachment(toEmail, subject, body, null);
    }
}
