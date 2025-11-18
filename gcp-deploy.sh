#!/bin/bash

# Script para desplegar todos los microservicios en Cloud Run
# Uso: ./gcp-deploy.sh [PROJECT_ID]

set -e

PROJECT_ID=${1:-"farmatodo-ecommerce"}
REGION=${REGION:-"us-central1"}

echo "🚀 Desplegando microservicios en Cloud Run..."
echo "📋 Proyecto: $PROJECT_ID"
echo "🌍 Región: $REGION"
echo ""

# Obtener información de Cloud SQL
DB_CONNECTION_NAME=$(gcloud sql instances describe farmatodo-db --format="value(connectionName)")
echo "📍 Cloud SQL Connection: $DB_CONNECTION_NAME"
echo ""

# Función para desplegar un servicio
deploy_service() {
    local SERVICE_NAME=$1
    local PORT=$2
    local ENV_VARS=$3
    
    echo "📦 Desplegando $SERVICE_NAME..."
    
    cd $SERVICE_NAME
    
    # Construir imagen
    echo "  🔨 Construyendo imagen..."
    gcloud builds submit --tag gcr.io/$PROJECT_ID/$SERVICE_NAME --project=$PROJECT_ID --quiet
    
    # Desplegar en Cloud Run
    echo "  🚀 Desplegando en Cloud Run..."
    gcloud run deploy $SERVICE_NAME \
        --image gcr.io/$PROJECT_ID/$SERVICE_NAME \
        --platform managed \
        --region $REGION \
        --allow-unauthenticated \
        --add-cloudsql-instances=$DB_CONNECTION_NAME \
        --set-env-vars="$ENV_VARS" \
        --set-secrets="DB_PASSWORD=db-password:latest,API_KEY=api-key:latest" \
        --memory=512Mi \
        --cpu=1 \
        --timeout=300 \
        --max-instances=10 \
        --project=$PROJECT_ID \
        --quiet
    
    cd ..
    
    echo "  ✅ $SERVICE_NAME desplegado"
    echo ""
}

# Desplegar servicios
echo "═══════════════════════════════════════════════════════════"
echo "Desplegando servicios..."
echo "═══════════════════════════════════════════════════════════"
echo ""

# Auth Service (tokenization-service)
deploy_service "auth-service" "8081" \
    "SERVER_PORT=8081,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,TOKEN_REJECTION_PROBABILITY=0.1,GCP_PROJECT_ID=$PROJECT_ID"

# Obtener URL de auth-service
AUTH_SERVICE_URL=$(gcloud run services describe auth-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
echo "📍 Auth Service URL: $AUTH_SERVICE_URL"
echo ""

# Customer Service
deploy_service "customer-service" "8082" \
    "SERVER_PORT=8082,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,GCP_PROJECT_ID=$PROJECT_ID"

# Product Service
deploy_service "product-service" "8083" \
    "SERVER_PORT=8083,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,MIN_STOCK_THRESHOLD=0,GCP_PROJECT_ID=$PROJECT_ID"

# Cart Service
deploy_service "cart-service" "8084" \
    "SERVER_PORT=8084,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,GCP_PROJECT_ID=$PROJECT_ID"

# Order Service
deploy_service "order-service" "8085" \
    "SERVER_PORT=8085,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,GCP_PROJECT_ID=$PROJECT_ID"

# Obtener URL de order-service
ORDER_SERVICE_URL=$(gcloud run services describe order-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
echo "📍 Order Service URL: $ORDER_SERVICE_URL"
echo ""

# Payment Worker
deploy_service "payment-worker" "8086" \
    "SERVER_PORT=8086,DB_HOST=/cloudsql/$DB_CONNECTION_NAME,DB_PORT=5432,DB_NAME=farmatodo,DB_USER=postgres,PAYMENT_REJECTION_PROBABILITY=0.2,MAX_PAYMENT_ATTEMPTS=3,ORDER_SERVICE_URL=$ORDER_SERVICE_URL,AUTH_SERVICE_URL=$AUTH_SERVICE_URL,GCP_PROJECT_ID=$PROJECT_ID"

# Notification Service
deploy_service "notification-service" "8087" \
    "SERVER_PORT=8087,MAIL_HOST=smtp.gmail.com,MAIL_PORT=587,GCP_PROJECT_ID=$PROJECT_ID"

echo "═══════════════════════════════════════════════════════════"
echo "✅ TODOS LOS SERVICIOS DESPLEGADOS"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "📋 URLs de servicios:"
gcloud run services list --region=$REGION --format="table(metadata.name,status.url)" --project=$PROJECT_ID
echo ""
echo "📝 Próximo paso: Configurar API Gateway"
echo "   Ejecuta: ./gcp-api-gateway-setup.sh $PROJECT_ID"
echo ""


