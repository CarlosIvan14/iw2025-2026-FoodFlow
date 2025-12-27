package pos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendReceiptWithPdf(String toEmail, byte[] pdfBytes, Long orderId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = permite anexo

            helper.setFrom("noreply@foodflow.com");
            helper.setTo(toEmail);

            // TRADUÇÃO AQUI:
            helper.setSubject("Su Recibo FoodFlow - Orden #" + orderId);
            helper.setText("¡Hola! Aquí tiene el recibo de su pedido adjunto. ¡Gracias por su compra!", false);

            // Nome do arquivo em espanhol
            helper.addAttachment("Recibo_Orden_" + orderId + ".pdf", new org.springframework.core.io.ByteArrayResource(pdfBytes));

            mailSender.send(message);
            log.info("Correo enviado a {}", toEmail);

        } catch (MessagingException e) {
            log.error("Fallo al enviar correo", e);
        }
    }
}