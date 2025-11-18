#!/bin/bash

# Script para configurar la infraestructura en Google Cloud Platform
# Uso: ./gcp-setup.sh [PROJECT_ID]

set -e

PROJECT_ID=${1:-"farmatodo-ecommerce"}
REGION=${REGION:-"us-central1"}

echo "🚀 Configurando infraestructura en GCP..."
echo "📋 Proyecto: $PROJECT_ID"
echo "🌍 Región: $REGION"
echo ""

# 1. Configurar proyecto
echo "1️⃣ Configurando proyecto..."
gcloud config set project $PROJECT_ID

# 2. Habilitar APIs necesarias
echo ""
echo "2️⃣ Habilitando APIs necesarias..."
gcloud services enable \
    cloudbuild.googleapis.com \
    run.googleapis.com \
    sqladmin.googleapis.com \
    pubsub.googleapis.com \
    secretmanager.googleapis.com \
    apigateway.googleapis.com \
    servicemanagement.googleapis.com \
    servicecontrol.googleapis.com

echo "✅ APIs habilitadas"

# 3. Crear Cloud SQL (PostgreSQL)
echo ""
echo "3️⃣ Creando Cloud SQL (PostgreSQL)..."
gcloud sql instances create farmatodo-db \
    --database-version=POSTGRES_15 \
    --tier=db-f1-micro \
    --region=$REGION \
    --root-password=postgres \
    --storage-type=SSD \
    --storage-size=10GB \
    --backup-start-time=03:00 \
    --enable-bin-log \
    --maintenance-window-day=SUN \
    --maintenance-window-hour=04 \
    --deletion-protection=false

echo "✅ Cloud SQL creado"

# 4. Crear base de datos
echo ""
echo "4️⃣ Creando base de datos..."
gcloud sql databases create farmatodo --instance=farmatodo-db

echo "✅ Base de datos creada"

# 5. Crear usuario de base de datos
echo ""
echo "5️⃣ Configurando usuario de base de datos..."
gcloud sql users create postgres \
    --instance=farmatodo-db \
    --password=postgres

echo "✅ Usuario creado"

# 6. Obtener connection name de Cloud SQL
echo ""
echo "6️⃣ Obteniendo información de Cloud SQL..."
DB_CONNECTION_NAME=$(gcloud sql instances describe farmatodo-db --format="value(connectionName)")
echo "📍 Cloud SQL Connection: $DB_CONNECTION_NAME"

# 7. Crear Pub/Sub topics
echo ""
echo "7️⃣ Creando Pub/Sub topics..."
gcloud pubsub topics create order-created --project=$PROJECT_ID
gcloud pubsub topics create payment-approved --project=$PROJECT_ID
gcloud pubsub topics create payment-rejected --project=$PROJECT_ID

echo "✅ Topics creados"

# 8. Crear Pub/Sub subscriptions
echo ""
echo "8️⃣ Creando Pub/Sub subscriptions..."
gcloud pubsub subscriptions create order-created-sub \
    --topic=order-created \
    --project=$PROJECT_ID

gcloud pubsub subscriptions create payment-approved-sub \
    --topic=payment-approved \
    --project=$PROJECT_ID

gcloud pubsub subscriptions create payment-rejected-sub \
    --topic=payment-rejected \
    --project=$PROJECT_ID

echo "✅ Subscriptions creadas"

# 9. Crear secrets en Secret Manager
echo ""
echo "9️⃣ Creando secrets en Secret Manager..."
echo -n "test-api-key-12345" | gcloud secrets create api-key \
    --data-file=- \
    --project=$PROJECT_ID \
    --replication-policy="automatic" 2>/dev/null || echo "Secret 'api-key' ya existe"

echo -n "postgres" | gcloud secrets create db-password \
    --data-file=- \
    --project=$PROJECT_ID \
    --replication-policy="automatic" 2>/dev/null || echo "Secret 'db-password' ya existe"

echo "✅ Secrets creados"

# 10. Dar permisos a Cloud Run para acceder a secrets
echo ""
echo "🔟 Configurando permisos..."
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
SERVICE_ACCOUNT="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

gcloud secrets add-iam-policy-binding api-key \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/secretmanager.secretAccessor" \
    --project=$PROJECT_ID 2>/dev/null || echo "Permisos de api-key ya configurados"

gcloud secrets add-iam-policy-binding db-password \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/secretmanager.secretAccessor" \
    --project=$PROJECT_ID 2>/dev/null || echo "Permisos de db-password ya configurados"

echo "✅ Permisos configurados"

# Resumen
echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ INFRAESTRUCTURA CREADA EXITOSAMENTE"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "📋 Resumen:"
echo "  • Proyecto: $PROJECT_ID"
echo "  • Cloud SQL: farmatodo-db"
echo "  • Connection Name: $DB_CONNECTION_NAME"
echo "  • Base de datos: farmatodo"
echo "  • Pub/Sub Topics: order-created, payment-approved, payment-rejected"
echo "  • Pub/Sub Subscriptions: order-created-sub, payment-approved-sub, payment-rejected-sub"
echo "  • Secrets: api-key, db-password"
echo ""
echo "📝 Próximos pasos:"
echo "  1. Ejecuta: ./gcp-deploy.sh $PROJECT_ID"
echo "  2. Configura API Gateway (ver gcp-api-gateway-setup.sh)"
echo ""


