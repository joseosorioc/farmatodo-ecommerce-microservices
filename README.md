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


La Arquitectura es la siguiente y se muestra en los diagramas: 

- En GCP


- La siguiente está más relacionada con la forma en que interactúan los componentes.

<img width="1496" height="961" alt="image" src="https://github.com/user-attachments/assets/8d3c04df-f27d-49a3-8817-239921fb3138" />


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

**Cobertura total**: 

<img width="1044" height="600" alt="image" src="https://github.com/user-attachments/assets/f88199c5-7d43-4665-a8d9-b335754953d6" />

<img width="1050" height="834" alt="image" src="https://github.com/user-attachments/assets/8681ef54-6e1c-420a-b4d5-704738bea5a0" />

<img width="1076" height="1098" alt="image" src="https://github.com/user-attachments/assets/fda30fc0-5502-41f7-9adb-33a9e5f57139" />

<img width="1056" height="806" alt="image" src="https://github.com/user-attachments/assets/e1495035-20d1-44c1-914f-9d7ddd813604" />

<img width="1070" height="858" alt="image" src="https://github.com/user-attachments/assets/1db6548b-abb3-49e9-8c96-0285e417c807" />

<img width="1048" height="624" alt="image" src="https://github.com/user-attachments/assets/2bbbeb4a-699b-4769-9ce1-4aec2bdb831e" />

<img width="1024" height="1204" alt="image" src="https://github.com/user-attachments/assets/07202f92-88d6-4f17-be84-7766d917b755" />


## 📚 Documentación API (Swagger/OpenAPI)

Cada microservicio incluye documentación Swagger/OpenAPI independiente. Esta arquitectura permite:

- **Independencia**: Cada servicio documenta sus propios endpoints
- **Escalabilidad**: Fácil agregar o modificar servicios sin afectar otros
- **Mantenibilidad**: Cambios en un servicio no afectan la documentación de otros
- **Claridad**: Documentación específica y enfocada por servicio

### Acceso a Swagger UI

Una vez que un servicio esté ejecutándose, puedes acceder a su documentación Swagger UI en:

| Servicio | URL Swagger UI | Puerto por defecto |
|----------|---------------|-------------------|
| auth-service | http://localhost:8080/swagger-ui.html | 8080 |
| cart-service | http://localhost:8080/swagger-ui.html | 8080* |
| customer-service | http://localhost:8080/swagger-ui.html | 8080* |
| order-service | http://localhost:8080/swagger-ui.html | 8080* |
| product-service | http://localhost:8080/swagger-ui.html | 8080 |
| notification-service | http://localhost:8080/swagger-ui.html | 8080* |

\* Los puertos pueden configurarse mediante la variable de entorno `PORT`

### Endpoints de Documentación

- **Swagger UI**: `/swagger-ui.html` o `/swagger-ui/index.html`
- **OpenAPI JSON**: `/v3/api-docs`
- **OpenAPI YAML**: `/v3/api-docs.yaml`

### Características

- Documentación interactiva de todos los endpoints
- Esquemas de request/response
- Validaciones y restricciones documentadas
- Autenticación configurada (API Key para auth-service)
- Ejemplos de uso
- Pruebas directas desde la interfaz

### Ejemplo de uso

1. Inicia el servicio:
```bash
cd auth-service
mvn spring-boot:run
```

2. Abre tu navegador en: `http://localhost:8080/swagger-ui.html`

3. Explora los endpoints y prueba las APIs directamente desde Swagger UI

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
- **SpringDoc OpenAPI** (Swagger/OpenAPI 3)
- **Lombok**
- **Maven**

## 📦 Estructura del Proyecto (Monorepositorio)

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
