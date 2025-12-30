package pos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Servicio para manejar la carga de imágenes de productos.
 * 
 * Soporta:
 * - Guardar archivos en carpeta local (por defecto: src/main/resources/static/uploads)
 * - Generar nombres únicos con UUID para evitar conflictos
 * - Validación de tipos de archivo permitidos
 * 
 * PARA PRODUCCIÓN EN S3:
 * Reemplaza los métodos con AWS SDK. Ver comentarios al final.
 */
@Service
public class ImageUploadService {

    // Carpeta donde se guardan las imágenes (por defecto: src/main/resources/static/uploads)
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // URL base para acceder a las imágenes desde el navegador
    @Value("${app.upload.url:/uploads}")
    private String uploadUrl;

    // Extensiones permitidas
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif"};

    /**
     * Sube una imagen desde MultipartFile y retorna la URL pública
     */
    public String uploadImage(MultipartFile file) throws IOException, IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validar tipo de archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Usa: jpg, jpeg, png, webp, gif");
        }

        // Crear carpeta si no existe
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Generar nombre único: UUID + extensión original
        String fileExtension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);

        // Guardar archivo
        Files.write(filePath, file.getBytes());

        // Retornar URL pública para acceder al archivo
        // Ej: /uploads/3fa85f64-5717-4562-b3fc-2c963f66afa6.jpg
        return uploadUrl + "/" + newFilename;
    }

    /**
     * Sube una imagen desde InputStream y retorna la URL pública
     * (Usado por Vaadin Upload que proporciona InputStream)
     */
    public String uploadImage(InputStream inputStream, String originalFilename) 
            throws IOException, IllegalArgumentException {
        if (inputStream == null || originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validar tipo de archivo
        if (!isAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Usa: jpg, jpeg, png, webp, gif");
        }

        // Crear carpeta si no existe
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Generar nombre único: UUID + extensión original
        String fileExtension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);

        // Guardar archivo desde InputStream
        Files.copy(inputStream, filePath);

        // Retornar URL pública para acceder al archivo
        return uploadUrl + "/" + newFilename;
    }

    /**
     * Elimina una imagen por su URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // Extraer nombre del archivo de la URL
            // Ej: de "/uploads/3fa85f64-5717-4562-b3fc-2c963f66afa6.jpg" → "3fa85f64-5717-4562-b3fc-2c963f66afa6.jpg"
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log del error pero no fallar el proceso
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }
    }

    /**
     * Valida si la extensión está permitida
     */
    private boolean isAllowedExtension(String filename) {
        String extension = getExtension(filename).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrae la extensión de un archivo
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * ============================================================
     * ALTERNATIVA: USAR AMAZON S3 PARA PRODUCCIÓN
     * ============================================================
     * 
     * Si quieres usar AWS S3 en lugar de carpeta local:
     * 
     * 1. Agregar dependencia en pom.xml:
     *    <dependency>
     *        <groupId>software.amazon.awssdk</groupId>
     *        <artifactId>s3</artifactId>
     *        <version>2.20.0</version>
     *    </dependency>
     * 
     * 2. Agregar propiedades en application.properties:
     *    aws.s3.bucket-name=mi-bucket-productos
     *    aws.s3.region=eu-west-1
     *    aws.accessKeyId=tu-access-key
     *    aws.secretAccessKey=tu-secret-key
     * 
     * 3. Código para S3:
     * 
     *    @Service
     *    public class ImageUploadService {
     *        private final S3Client s3Client;
     *        
     *        @Value("${aws.s3.bucket-name}")
     *        private String bucketName;
     *        
     *        public String uploadImage(MultipartFile file) throws IOException {
     *            String key = "productos/" + UUID.randomUUID() + ".jpg";
     *            
     *            s3Client.putObject(
     *                PutObjectRequest.builder()
     *                    .bucket(bucketName)
     *                    .key(key)
     *                    .build(),
     *                RequestBody.fromBytes(file.getBytes())
     *            );
     *            
     *            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
     *        }
     *    }
     * 
     * ============================================================
     */
}

