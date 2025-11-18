# farmatodo-ecommerce-microservices

Proyecto para prueba backend en Farmatodo. Arquitectura de microservicios para e-commerce.

## 📋 Descripción

Sistema de e-commerce basado en microservicios que incluye gestión de productos, carrito de compras, pedidos, pagos, clientes, autenticación y notificaciones.

## 🏗️ Arquitectura

El proyecto está compuesto por los siguientes microservicios:

### Servicios Principales

1. **auth-service** - Servicio de autenticación y tokenización de tarjetas de crédito
2. **cart-service** - Servicio de gestión de carrito de compras
3. **customer-service** - Servicio de gestión de clientes
4. **order-service** - Servicio de gestión de pedidos
5. **product-service** - Servicio de gestión de productos y búsqueda
6. **payment-worker** - Worker para procesamiento de pagos
7. **notification-service** - Servicio de notificaciones por correo electrónico

## 🧪 Pruebas Unitarias

Todos los servicios incluyen pruebas unitarias completas usando JUnit 5 y Mockito.

### Cobertura de Pruebas

#### auth-service
- **TokenServiceTest**: 5 pruebas
  - Creación de tokens exitosa
  - Obtención de tokens
  - Rechazo de tokenización
  - Enmascaramiento de números de tarjeta
- **AuthControllerTest**: 4 pruebas
  - Endpoint ping
  - Creación de tokens
  - Validaciones
  - Manejo de errores

#### cart-service
- **CartServiceTest**: 6 pruebas
  - Agregar items al carrito (nuevo y existente)
  - Obtener items del carrito
  - Eliminar items
  - Limpiar carrito
- **CartControllerTest**: 5 pruebas
  - Todos los endpoints CRUD
  - Validaciones

#### customer-service
- **CustomerServiceTest**: 8 pruebas
  - Crear cliente
  - Obtener cliente por ID
  - Obtener todos los clientes
  - Actualizar cliente
  - Validaciones de email y teléfono únicos
- **CustomerControllerTest**: 5 pruebas
  - Todos los endpoints REST
  - Validaciones

#### notification-service
- **EmailServiceTest**: 5 pruebas
  - Envío de emails de éxito y fallo
  - Manejo de excepciones
  - Validación de contenido
- **NotificationControllerTest**: 1 prueba
  - Endpoint ping
- **NotificationEventListenerTest**: 3 pruebas
  - Procesamiento de mensajes Pub/Sub
  - Manejo de errores

#### order-service
- **OrderServiceTest**: 7 pruebas
  - Crear pedido
  - Obtener pedido por ID
  - Obtener pedidos por cliente
  - Actualizar estado de pedido
  - Actualizar token de tarjeta
- **OrderControllerTest**: 5 pruebas
  - Todos los endpoints REST
  - Validaciones

#### payment-worker
- **PaymentServiceTest**: 5 pruebas
  - Procesamiento de pagos exitoso
  - Tokenización rechazada
  - Pago rechazado
  - Errores de conexión
  - Reintentos
- **PaymentEventListenerTest**: 4 pruebas
  - Procesamiento de mensajes Pub/Sub
  - Manejo de JSON inválido
  - Manejo de excepciones

#### product-service
- **ProductServiceTest**: 16 pruebas
  - Búsqueda de productos
  - Obtener todos los productos
  - Obtener producto por ID
  - Filtrado por stock
  - Guardado asíncrono de búsquedas
  - Manejo de excepciones
- **ProductControllerTest**: 7 pruebas
  - Todos los endpoints REST
  - Búsqueda con y sin customerId
  - Manejo de errores

**Cobertura total**: Más de 90% en product-service, cobertura completa en todos los servicios principales.

## 🚀 Ejecución de Pruebas

### Ejecutar todas las pruebas de un servicio

```bash
cd auth-service
mvn test
```

### Ejecutar pruebas con cobertura (JaCoCo)

```bash
cd product-service
mvn clean test
mvn jacoco:report
```

El reporte de cobertura se generará en: `target/site/jacoco/index.html`

### Ejecutar pruebas de un servicio específico

```bash
cd auth-service
mvn test -Dtest=TokenServiceTest
```

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Google Cloud Pub/Sub**
- **JUnit 5**
- **Mockito**
- **JaCoCo** (Cobertura de código)
- **Lombok**
- **Maven**

## 📦 Estructura del Proyecto

```
farmatodo-ecommerce-microservices/
├── auth-service/
├── cart-service/
├── customer-service/
├── notification-service/
├── order-service/
├── payment-worker/
├── product-service/
└── README.md
```

Cada servicio sigue la estructura estándar de Spring Boot:
- `src/main/java/` - Código fuente
- `src/test/java/` - Pruebas unitarias
- `pom.xml` - Configuración Maven

## 🔧 Configuración

Cada servicio tiene su propio archivo `application.yml` con la configuración necesaria. Para desarrollo local, se puede usar H2 como base de datos en pruebas.

## 📝 Notas

- Las pruebas unitarias utilizan mocks para aislar las dependencias externas
- Los servicios de Pub/Sub están mockeados en las pruebas
- La cobertura de código se genera automáticamente con JaCoCo
- Todas las pruebas están escritas siguiendo el patrón Arrange-Act-Assert (AAA)
