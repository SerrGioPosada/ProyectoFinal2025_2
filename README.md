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

## 🏗️ Arquitectura

El proyecto sigue una arquitectura en capas:

```
Controller → Service → Repository → Model
```

- **Controller**: Controladores JavaFX que manejan la lógica de la UI
- **Service**: Lógica de negocio y orquestación
- **Repository**: Acceso a datos (persistencia en JSON usando patrón Singleton)
- **Model**: Entidades del dominio

## 🎨 Patrones de Diseño

### 1. Singleton
**Propósito:** Garantizar una única instancia de cada repositorio.

**Implementación:** Todos los repositorios (`UserRepository`, `AdminRepository`, `OrderRepository`, etc.)

**Uso:**
```java
UserRepository userRepo = UserRepository.getInstance();
userRepo.save(newUser);
```

### 2. Factory
**Propósito:** Centralizar la creación de diferentes tipos de personas.

**Implementación:** `PersonFactory` en `Model/Factory/`

**Uso:**
```java
User user = (User) PersonFactory.createPerson(PersonType.USER, "John", "Doe", "12345", "john@email.com", "password");
Admin admin = (Admin) PersonFactory.createPerson(PersonType.ADMIN, "Admin", "User", "99999", "admin@uq.edu.co", "admin123");
```

### 3. Decorator
**Propósito:** Añadir funcionalidades adicionales (como seguro) al costo base de envío.

**Implementación:** `BaseShippingCost`, `InsuranceDecorator` en `Model/Decorator/`

**Uso:**
```java
ShippingCost baseCost = new BaseShippingCost(shipment);
ShippingCost withInsurance = new InsuranceDecorator(baseCost);
double totalCost = withInsurance.calculateCost(); // Costo base + seguro
```

### 4. Strategy
**Propósito:** Calcular tarifas de forma dinámica según el tipo de vehículo.

**Implementación:** `TariffCalculationStrategy` con implementaciones por tipo de vehículo en `Model/Strategy/`

**Uso:**
```java
TariffCalculationStrategy strategy = tariff.getCalculationStrategy();
double cost = strategy.calculateCost(distance, weight);
```

### 5. Observer
**Propósito:** Notificar a los repartidores cuando cambia el estado de un envío.

**Implementación:** `ShipmentSubject`, `ShipmentObserver` en `Model/Observer/`

**Uso:**
```java
Shipment shipment = new Shipment(); // ShipmentSubject
DeliveryPerson deliveryPerson = new DeliveryPerson(); // ShipmentObserver
shipment.addObserver(deliveryPerson);
shipment.setStatus(ShipmentStatus.IN_TRANSIT); // Notifica automáticamente
```

## 📁 Estructura del Proyecto

```
src/main/java/
├── Controller/          # Controladores JavaFX
├── Model/              # Entidades del dominio
│   ├── Decorator/      # Patrón Decorator para costos
│   ├── Factory/        # Patrón Factory para personas
│   ├── Observer/       # Patrón Observer para shipments
│   ├── Strategy/       # Patrón Strategy para tarifas
│   ├── Enums/          # Estados y tipos
│   └── dto/            # Data Transfer Objects
├── Services/           # Lógica de negocio
├── Repositories/       # Persistencia (JSON con Singleton)
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
- Email: `admin@sistema.com`
- Contraseña: `Admin123!`

## 📚 Documentación

- **[Pensamiento Computacional](https://docs.google.com/document/d/1VCakIe6wl78RwUaXCmq1-bJTf_DDvGoO-xC6zd-FCLg/edit?usp=sharing)** - Análisis del problema y diseño de la solución
- **[Diagrama de Clases](https://lucid.app/lucidchart/88013a27-c698-4c9a-94f7-c0c10046051b/edit?viewport_loc=1763%2C-574%2C4243%2C2317%2C0_0&invitationId=inv_aac7c7c6-d037-455a-a121-0b79140aebaa)** - Arquitectura completa del sistema

## 🛡️ Autores

* **Sergio Posada Garcia** - [SerrGioPosada](https://github.com/SerrGioPosada)
* **[Michael Joel Alvarez Gil]** - [@NombreUsuarioGit2](https://github.com/NombreUsuarioGit2)
* **[Juan Sebastian Mateus Hernandez]** - [Sebastian-Mateus](https://github.com/Sebastian-Mateus)

## 🤝 Contribución

Este es un proyecto académico de la Universidad del Quindío.

---
