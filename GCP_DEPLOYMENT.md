# Guía de Despliegue en Google Cloud Platform

Esta guía te ayudará a desplegar toda la infraestructura y microservicios en GCP según la arquitectura definida.

## 📋 Arquitectura en GCP

```
API Gateway (entrypoint HTTP público)
    │
    ├── product-service (Cloud Run)
    ├── customer-service (Cloud Run)
    ├── cart-service (Cloud Run)
    └── order-service (Cloud Run)
            │
            │ crea orden, publica evento
            ▼
    Pub/Sub topic: order-created
            │
            ▼
    payment-worker (Cloud Run)
    (consume order-created, llama tokenization-service, decide)
            │
    ┌───────┴────────┐
    ▼                ▼
tokenization-service   Pub/Sub payment-approved /
(auth-service)        payment-rejected topics
(Cloud Run)                    │
                               ▼
                    notification-service (Cloud Run)
```

## 🚀 Pasos de Despliegue

### Paso 1: Prerequisitos

1. **Instalar Google Cloud SDK:**
   ```bash
   # macOS
   brew install google-cloud-sdk
   
   # O descargar desde: https://cloud.google.com/sdk/docs/install
   ```

2. **Autenticarse:**
   ```bash
   gcloud auth login
   gcloud auth application-default login
   ```

3. **Crear proyecto en GCP:**
   ```bash
   gcloud projects create farmatodo-ecommerce
   gcloud config set project farmatodo-ecommerce
   ```

### Paso 2: Crear Infraestructura

Ejecuta el script que crea toda la infraestructura:

```bash
./gcp-setup.sh farmatodo-ecommerce
```

Este script crea:
- ✅ Cloud SQL (PostgreSQL)
- ✅ Pub/Sub Topics y Subscriptions
- ✅ Secret Manager (para API keys y passwords)
- ✅ Permisos necesarios

### Paso 3: Desplegar Microservicios

Ejecuta el script que despliega todos los servicios en Cloud Run:

```bash
./gcp-deploy.sh farmatodo-ecommerce
```

Este script:
- ✅ Construye imágenes Docker de cada servicio
- ✅ Las sube a Google Container Registry
- ✅ Las despliega en Cloud Run
- ✅ Configura conexión a Cloud SQL
- ✅ Configura variables de entorno
- ✅ Conecta a Pub/Sub

### Paso 4: Configurar API Gateway

Ejecuta el script que configura el API Gateway:

```bash
./gcp-api-gateway-setup.sh farmatodo-ecommerce
```

Este script:
- ✅ Crea la API en API Gateway
- ✅ Configura rutas a cada microservicio
- ✅ Crea el Gateway público

## 📝 Configuración de Servicios

### Variables de Entorno en Cloud Run

Los servicios se configuran automáticamente con:

- **DB_HOST**: `/cloudsql/[CONNECTION_NAME]` (Cloud SQL Unix socket)
- **DB_PASSWORD**: Desde Secret Manager
- **API_KEY**: Desde Secret Manager
- **GCP_PROJECT_ID**: Tu proyecto de GCP
- **PUBSUB_ENABLED**: `true` (automático cuando GCP_PROJECT_ID != "local")

### URLs de Servicios

Después del despliegue, obtén las URLs:

```bash
gcloud run services list --region=us-central1
```

## 🔧 Comandos Útiles

### Ver logs de un servicio:
```bash
gcloud run services logs read auth-service --region=us-central1
```

### Actualizar un servicio:
```bash
cd auth-service
gcloud builds submit --tag gcr.io/farmatodo-ecommerce/auth-service
gcloud run deploy auth-service --image gcr.io/farmatodo-ecommerce/auth-service --region=us-central1
```

### Ver estado de Cloud SQL:
```bash
gcloud sql instances describe farmatodo-db
```

### Ver topics de Pub/Sub:
```bash
gcloud pubsub topics list
```

### Ver subscriptions:
```bash
gcloud pubsub subscriptions list
```

## 🧪 Probar el Despliegue

1. **Obtener URL del API Gateway:**
   ```bash
   gcloud api-gateway gateways describe farmatodo-api-gateway \
     --location=us-central1 \
     --format="value(defaultHostname)"
   ```

2. **Probar endpoint:**
   ```bash
   curl https://[GATEWAY_URL]/api/v1/ping
   ```

3. **Actualizar colección de Postman:**
   - Cambia `{{base_url}}` por la URL del API Gateway
   - Prueba todos los endpoints

## 💰 Costos Estimados

- **Cloud Run**: Pay per use (muy económico para desarrollo)
- **Cloud SQL**: ~$7-10/mes (db-f1-micro)
- **Pub/Sub**: Primeros 10GB/mes gratis
- **API Gateway**: Primeros 2 millones de requests/mes gratis

## 🔒 Seguridad

- Los secrets están en Secret Manager
- Cloud SQL solo acepta conexiones desde Cloud Run
- API Gateway maneja autenticación y rate limiting
- Cada servicio tiene su propia service account

## 📚 Documentación Adicional

- [Cloud Run Docs](https://cloud.google.com/run/docs)
- [Cloud SQL Docs](https://cloud.google.com/sql/docs)
- [Pub/Sub Docs](https://cloud.google.com/pubsub/docs)
- [API Gateway Docs](https://cloud.google.com/api-gateway/docs)


