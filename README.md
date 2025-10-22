# Sistema de Gestión de Envíos 📦

Sistema completo de gestión de envíos desarrollado con JavaFX 21 y Maven. Implementa autenticación, gestión de usuarios, administradores, repartidores, órdenes y envíos con integración de Google OAuth2.

## 🚀 Tecnologías

- **Java 25** - Amazon Corretto 25.0.1
- **JavaFX 21** - Framework de interfaz gráfica
- **Maven** - Gestión de dependencias
- **Lombok 1.18.40** - Reducción de boilerplate
- **Gson** - Serialización JSON
- **JBcrypt** - Hashing de contraseñas
- **Google OAuth2** - Autenticación con Google

## 📋 Requisitos

- **JDK 25** (Amazon Corretto recomendado)
- **Maven** (incluido vía wrapper)

## 🔨 Compilación y Ejecución

### Compilar:
```bash
./mvnw clean compile
```

### Ejecutar:
```bash
./mvnw javafx:run
```

### Ejecutar desde IntelliJ IDEA:
1. Abre el proyecto en IntelliJ
2. Selecciona la configuración "MainApp" en el dropdown
3. Haz clic en Run ▶️

> **Nota:** Los warnings de JavaFX están configurados para eliminarse automáticamente mediante `.mvn/jvm.config` y la configuración de IntelliJ.

## 🏗️ Arquitectura

```
Controller → Service → Repository → Model
```

### Patrones de Diseño Implementados

- **Singleton**: Repositories
- **Factory**: Creación de personas (User/Admin/DeliveryPerson)
- **Decorator**: Cálculo de costos de envío
- **Strategy**: Tarifas por tipo de vehículo
- **Observer**: Notificaciones de cambios en shipments

## 📁 Estructura del Proyecto

```
src/main/java/
├── Controller/          # Controladores JavaFX
├── Model/              # Entidades del dominio
│   ├── Decorator/      # Patrón Decorator
│   ├── Factory/        # Patrón Factory
│   ├── Observer/       # Patrón Observer
│   ├── Strategy/       # Patrón Strategy
│   ├── Enums/          # Estados y tipos
│   └── dto/            # Data Transfer Objects
├── Services/           # Lógica de negocio
├── Repositories/       # Persistencia (JSON)
└── Util/              # Utilidades

src/main/resources/
└── View/              # Archivos FXML
```

## 💾 Persistencia

Los datos se almacenan en archivos JSON en el directorio `data/`:
- `data/admins.json` - Administradores
- `data/users.json` - Usuarios regulares
- `data/tariffs.json` - Tarifas de envío

## 🔐 Autenticación

- Login tradicional (email/password con BCrypt)
- Integración con Google OAuth2
- Sesión gestionada por `AuthenticationService`

### Configuración de OAuth (Incluida)

> **⚠️ IMPORTANTE - USO EDUCATIVO:** Las credenciales de Google OAuth están incluidas en el archivo `config/oauth.properties` para facilitar la evaluación del proyecto universitario. **NO usar estas credenciales en producción.**

**La aplicación funciona inmediatamente después de clonar el repositorio** - no se requiere configuración adicional.

**Para producción:** Crea tus propias credenciales en [Google Cloud Console](https://console.cloud.google.com/) y actualiza `config/oauth.properties`.

## 👥 Usuario por Defecto

**Administrador:**
- Email: `admin@uq.edu.co`
- Contraseña: `admin123`

## 🌿 Branch de Desarrollo

La rama de desarrollo principal es `dev` (no `main`).

## 📝 Notas

- El proyecto usa Java Module System (ver `module-info.java`)
- La carpeta `data/` no se sube al repositorio (datos locales)
- Configuración de OAuth incluida para facilitar evaluación universitaria

## 🤝 Contribución

Este es un proyecto académico de la Universidad del Quindío.

---