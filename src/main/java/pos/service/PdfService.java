package pos.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import pos.domain.Order;
import pos.domain.OrderItem;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] generateReceipt(Order order) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            // Título
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("FoodFlow - Recibo", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Espaço vazio

            // Info do Pedido (Traduzido e com Hora)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            document.add(new Paragraph("Orden #" + order.getId()));
            // MUDANÇA: Agora mostra Data e Hora
            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(formatter)));

            // REMOVIDO: O bloco que mostrava o nome do cliente foi apagado.

            document.add(new Paragraph(" "));

            // Tabela de Itens (Cabeçalhos em Espanhol)
            PdfPTable table = new PdfPTable(3); // 3 colunas
            table.setWidthPercentage(100);

            // Estilizando o cabeçalho da tabela
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new Phrase("Producto", fontHeader));
            table.addCell(new Phrase("Cant.", fontHeader));
            table.addCell(new Phrase("Precio", fontHeader));

            // Formatador de Moeda (Espanha/Euro)
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getProductName());
                table.addCell(item.getQuantity().toString());
                table.addCell(currency.format(item.getTotal()));
            }

            document.add(table);

            // Total (Em Espanhol)
            document.add(new Paragraph(" "));
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph total = new Paragraph("TOTAL: " + currency.format(order.getTotal()), fontTotal);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Rodapé (Mensagem em Espanhol)
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("¡Gracias por su visita!", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
}