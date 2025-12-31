package pos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
 * Servicio para subir imágenes a Amazon S3
 * Lee credenciales del .env o variables de entorno del sistema
 */
@Service
public class S3ImageUploadService {

    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretAccessKey;

    @Value("${AWS_REGION:eu-north-1}")
    private String region;

    @Value("${S3_BUCKET_NAME:}")
    private String bucketName;

    @Value("${S3_PRODUCTS_PREFIX:products/}")
    private String productsPrefix;

    private S3Client s3Client;

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif"};

    /**
     * Inicializa el cliente S3 después de que Spring inyecte las propiedades
     */
    @PostConstruct
    public void initializeS3Client() {
        if (accessKeyId == null || accessKeyId.isBlank() || 
            secretAccessKey == null || secretAccessKey.isBlank()) {
            System.err.println("⚠️ ADVERTENCIA: Credenciales de AWS no configuradas. Verifica tu .env");
            return;
        }

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    accessKeyId,
                    secretAccessKey
            );

            Region awsRegion = Region.of(region);

            this.s3Client = S3Client.builder()
                    .region(awsRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            System.out.println("✓ Cliente S3 inicializado correctamente");
            System.out.println("  Bucket: " + bucketName);
            System.out.println("  Region: " + region);

        } catch (Exception e) {
            System.err.println("✗ Error al inicializar cliente S3: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar S3", e);
        }
    }

    /**
     * Sube una imagen a S3 y retorna la URL pública
     */
    public String uploadImage(MultipartFile file) throws Exception {
        if (s3Client == null) {
            throw new IllegalStateException("Cliente S3 no inicializado. Verifica credenciales en .env");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String originalFilename = file.getOriginalFilename();
        if (!isAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Usa: jpg, jpeg, png, webp, gif");
        }

        // Generar nombre único: UUID + extensión
        String fileExtension = getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        String s3Key = productsPrefix + uniqueFilename;

        try {
            // Subir a S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromBytes(file.getBytes()));

            // Generar URL pública
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, s3Key);

            System.out.println("✓ Imagen subida a S3: " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.err.println("✗ Error al subir imagen a S3: " + e.getMessage());
            throw new Exception("Error al subir imagen a S3: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una imagen de S3
     */
    public void deleteImage(String imageUrl) {
        if (s3Client == null || imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // Extraer la clave S3 de la URL
            // Ej: https://foodlow-images.s3.eu-north-1.amazonaws.com/products/uuid.jpg → products/uuid.jpg
            String s3Key = imageUrl.substring(imageUrl.indexOf(".com/") + 5);

            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());

            System.out.println("✓ Imagen eliminada de S3: " + s3Key);

        } catch (Exception e) {
            System.err.println("⚠️ Error al eliminar imagen de S3: " + e.getMessage());
        }
    }

    /**
     * Valida si la extensión está permitida
     */
    private boolean isAllowedExtension(String filename) {
        if (filename == null) return false;
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
}
