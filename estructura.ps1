Write-Host "🚀 Creando estructura del proyecto POS (Vaadin + Spring Boot)..."

# Rutas base
$base = "src/main/java/pos"
$resources = "src/main/resources"

# Crear carpetas principales
New-Item -ItemType Directory -Force -Path "$base/security" | Out-Null
New-Item -ItemType Directory -Force -Path "$base/domain/model" | Out-Null
New-Item -ItemType Directory -Force -Path "$base/domain/repository" | Out-Null
New-Item -ItemType Directory -Force -Path "$base/domain/service" | Out-Null
New-Item -ItemType Directory -Force -Path "$base/infra/mock" | Out-Null
New-Item -ItemType Directory -Force -Path "$base/ui/views" | Out-Null
New-Item -ItemType Directory -Force -Path $resources | Out-Null

# Crear archivo de configuración
New-Item -ItemType File -Force -Path "$resources/application.properties" | Out-Null

# Crear clase principal
$mainClass = @'
package pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}
'@

$mainPath = "$base/PosApplication.java"
$mainClass | Out-File -Encoding UTF8 $mainPath

Write-Host "✅ Estructura creada correctamente:"
Write-Host "   - Base package: pos"
Write-Host "   - Carpetas de seguridad, dominio, infraestructura y vistas listas."
Write-Host "   - Archivo principal: $mainPath"
Write-Host "   - Archivo de propiedades: $resources\\application.properties"
Write-Host "`n🎯 Ahora puedes copiar el código de cada clase y ejecutar:"
Write-Host "   ./mvnw spring-boot:run"
