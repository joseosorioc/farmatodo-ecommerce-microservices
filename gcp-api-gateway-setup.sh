#!/bin/bash

# Script para configurar API Gateway en GCP
# Uso: ./gcp-api-gateway-setup.sh [PROJECT_ID]

set -e

PROJECT_ID=${1:-"farmatodo-ecommerce"}
REGION=${REGION:-"us-central1"}
GATEWAY_NAME="farmatodo-api-gateway"
API_ID="farmatodo-api"

echo "🚀 Configurando API Gateway..."
echo "📋 Proyecto: $PROJECT_ID"
echo "🌍 Región: $REGION"
echo ""

# Obtener URLs de servicios
echo "📍 Obteniendo URLs de servicios..."
AUTH_SERVICE_URL=$(gcloud run services describe auth-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
CUSTOMER_SERVICE_URL=$(gcloud run services describe customer-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
PRODUCT_SERVICE_URL=$(gcloud run services describe product-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
CART_SERVICE_URL=$(gcloud run services describe cart-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)
ORDER_SERVICE_URL=$(gcloud run services describe order-service --region=$REGION --format="value(status.url)" --project=$PROJECT_ID)

echo "✅ URLs obtenidas"
echo ""

# Obtener PROJECT_NUMBER
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

# Crear archivo de configuración de API Gateway
echo "📝 Creando configuración de API Gateway..."
cat > api-gateway-config.yaml <<EOF
swagger: '2.0'
info:
  title: Farmatodo E-Commerce API
  description: API Gateway para microservicios Farmatodo
  version: 1.0.0
host: ${GATEWAY_NAME}-${PROJECT_NUMBER}.a.run.app
schemes:
  - https
produces:
  - application/json
paths:
  /api/v1/ping:
    get:
      summary: Health check
      operationId: ping
      x-google-backend:
        address: ${AUTH_SERVICE_URL}/api/v1/ping
      responses:
        '200':
          description: OK
  
  /api/v1/tokens:
    post:
      summary: Crear token de tarjeta
      operationId: createToken
      parameters:
        - in: header
          name: X-API-Key
          required: true
          type: string
        - in: body
          name: body
          required: true
          schema:
            type: object
      x-google-backend:
        address: ${AUTH_SERVICE_URL}/api/v1/tokens
      responses:
        '201':
          description: Token creado
        '400':
          description: Error de validación
        '401':
          description: No autorizado
  
  /api/v1/customers:
    get:
      summary: Listar clientes
      operationId: listCustomers
      x-google-backend:
        address: ${CUSTOMER_SERVICE_URL}/api/v1/customers
      responses:
        '200':
          description: OK
    post:
      summary: Crear cliente
      operationId: createCustomer
      parameters:
        - in: body
          name: body
          required: true
          schema:
            type: object
      x-google-backend:
        address: ${CUSTOMER_SERVICE_URL}/api/v1/customers
      responses:
        '201':
          description: Cliente creado
  
  /api/v1/customers/{id}:
    get:
      summary: Obtener cliente
      operationId: getCustomer
      parameters:
        - in: path
          name: id
          required: true
          type: string
      x-google-backend:
        address: ${CUSTOMER_SERVICE_URL}/api/v1/customers/{id}
      responses:
        '200':
          description: OK
  
  /api/v1/products:
    get:
      summary: Listar productos
      operationId: listProducts
      x-google-backend:
        address: ${PRODUCT_SERVICE_URL}/api/v1/products
      responses:
        '200':
          description: OK
  
  /api/v1/products/search:
    get:
      summary: Buscar productos
      operationId: searchProducts
      parameters:
        - in: query
          name: query
          type: string
        - in: query
          name: customerId
          type: string
      x-google-backend:
        address: ${PRODUCT_SERVICE_URL}/api/v1/products/search
      responses:
        '200':
          description: OK
  
  /api/v1/products/{id}:
    get:
      summary: Obtener producto
      operationId: getProduct
      parameters:
        - in: path
          name: id
          required: true
          type: string
      x-google-backend:
        address: ${PRODUCT_SERVICE_URL}/api/v1/products/{id}
      responses:
        '200':
          description: OK
  
  /api/v1/carts/{customerId}/items:
    get:
      summary: Obtener items del carrito
      operationId: getCartItems
      parameters:
        - in: path
          name: customerId
          required: true
          type: string
      x-google-backend:
        address: ${CART_SERVICE_URL}/api/v1/carts/{customerId}/items
      responses:
        '200':
          description: OK
    post:
      summary: Agregar item al carrito
      operationId: addCartItem
      parameters:
        - in: path
          name: customerId
          required: true
          type: string
        - in: body
          name: body
          required: true
          schema:
            type: object
      x-google-backend:
        address: ${CART_SERVICE_URL}/api/v1/carts/{customerId}/items
      responses:
        '200':
          description: OK
  
  /api/v1/carts/{customerId}/items/{productId}:
    delete:
      summary: Eliminar item del carrito
      operationId: removeCartItem
      parameters:
        - in: path
          name: customerId
          required: true
          type: string
        - in: path
          name: productId
          required: true
          type: string
      x-google-backend:
        address: ${CART_SERVICE_URL}/api/v1/carts/{customerId}/items/{productId}
      responses:
        '200':
          description: OK
  
  /api/v1/carts/{customerId}:
    delete:
      summary: Limpiar carrito
      operationId: clearCart
      parameters:
        - in: path
          name: customerId
          required: true
          type: string
      x-google-backend:
        address: ${CART_SERVICE_URL}/api/v1/carts/{customerId}
      responses:
        '200':
          description: OK
  
  /api/v1/orders:
    post:
      summary: Crear pedido
      operationId: createOrder
      parameters:
        - in: body
          name: body
          required: true
          schema:
            type: object
      x-google-backend:
        address: ${ORDER_SERVICE_URL}/api/v1/orders
      responses:
        '201':
          description: Pedido creado
  
  /api/v1/orders/{id}:
    get:
      summary: Obtener pedido
      operationId: getOrder
      parameters:
        - in: path
          name: id
          required: true
          type: string
      x-google-backend:
        address: ${ORDER_SERVICE_URL}/api/v1/orders/{id}
      responses:
        '200':
          description: OK
  
  /api/v1/orders/customer/{customerId}:
    get:
      summary: Obtener pedidos por cliente
      operationId: getOrdersByCustomer
      parameters:
        - in: path
          name: customerId
          required: true
          type: string
      x-google-backend:
        address: ${ORDER_SERVICE_URL}/api/v1/orders/customer/{customerId}
      responses:
        '200':
          description: OK
EOF

echo "✅ Configuración creada"
echo ""

# Crear API
echo "📝 Creando API..."
gcloud api-gateway apis create $API_ID \
    --project=$PROJECT_ID \
    --display-name="Farmatodo E-Commerce API" 2>/dev/null || echo "API ya existe"

echo "✅ API creada"
echo ""

# Crear configuración de API
echo "📝 Creando configuración de API..."
gcloud api-gateway api-configs create farmatodo-api-config \
    --api=$API_ID \
    --openapi-spec=api-gateway-config.yaml \
    --project=$PROJECT_ID \
    --backend-auth-service-account=${PROJECT_NUMBER}-compute@developer.gserviceaccount.com

echo "✅ Configuración de API creada"
echo ""

# Crear Gateway
echo "📝 Creando Gateway..."
gcloud api-gateway gateways create $GATEWAY_NAME \
    --api=$API_ID \
    --api-config=farmatodo-api-config \
    --location=$REGION \
    --project=$PROJECT_ID 2>/dev/null || echo "Gateway ya existe"

echo "✅ Gateway creado"
echo ""

# Obtener URL del Gateway
GATEWAY_URL=$(gcloud api-gateway gateways describe $GATEWAY_NAME --location=$REGION --format="value(defaultHostname)" --project=$PROJECT_ID)

echo "═══════════════════════════════════════════════════════════"
echo "✅ API GATEWAY CONFIGURADO"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "🌐 URL del API Gateway:"
echo "   https://$GATEWAY_URL"
echo ""
echo "📝 Prueba el endpoint:"
echo "   curl https://$GATEWAY_URL/api/v1/ping"
echo ""


