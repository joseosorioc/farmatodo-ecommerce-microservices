# Instrucciones para Importar el Diagrama en Draw.io

## 📥 Opción 1: Importar el archivo XML directamente

1. Abre tu navegador y ve a [https://app.diagrams.net/](https://app.diagrams.net/) (Draw.io)
2. Haz clic en **"Open Existing Diagram"** o **"Abrir diagrama existente"**
3. Selecciona la opción **"Device"** o **"Dispositivo"**
4. Navega hasta el archivo `ARQUITECTURA_GCP_DRAWIO.xml` en tu proyecto
5. Haz clic en **"Abrir"**
6. El diagrama se cargará automáticamente

## 📥 Opción 2: Arrastrar y soltar

1. Abre [https://app.diagrams.net/](https://app.diagrams.net/)
2. Arrastra el archivo `ARQUITECTURA_GCP_DRAWIO.xml` directamente a la ventana del navegador
3. El diagrama se cargará automáticamente

## 📥 Opción 3: Crear desde cero usando la descripción

Si prefieres crear el diagrama manualmente o el archivo XML no funciona correctamente, sigue estos pasos:

### Componentes a crear:

#### 1. Capas del Diagrama (de arriba hacia abajo):

**Capa 1: Cliente y API Gateway**
- Actor "Cliente/Internet" (izquierda)
- Rectángulo "API Gateway (Cloud API Gateway)" (centro)

**Capa 2: Cloud Run Services**
- Rectángulo contenedor "Cloud Run Services" (fondo amarillo)
- Dentro, 5 servicios principales:
  - auth-service (verde)
  - customer-service (verde)
  - product-service (verde)
  - cart-service (verde)
  - order-service (verde)

**Capa 3: Workers/Procesadores**
- payment-worker (rojo/naranja)
- notification-service (rojo/naranja)

**Capa 4: Cloud Pub/Sub**
- Rectángulo contenedor "Cloud Pub/Sub" (fondo morado)
- 3 Topics:
  - order-created
  - payment-approved
  - payment-rejected
- 3 Subscriptions (debajo de cada topic):
  - order-created-sub
  - payment-approved-sub
  - payment-rejected-sub

**Capa 5: Infraestructura**
- Cloud SQL (PostgreSQL) - naranja
- Secret Manager - amarillo
- Container Registry - amarillo
- Cloud Build - amarillo
- SMTP Server (externo) - gris con borde punteado

#### 2. Conexiones (Flechas):

**Flechas azules (HTTP/HTTPS):**
- Cliente → API Gateway
- API Gateway → cada servicio Cloud Run

**Flechas naranjas (Pub/Sub):**
- order-service → topic order-created
- topic order-created → subscription → payment-worker
- payment-worker → topics payment-approved y payment-rejected
- subscriptions → notification-service

**Flechas rojas (Llamadas HTTP internas):**
- payment-worker → auth-service

**Flechas punteadas naranjas (JDBC):**
- Todos los servicios → Cloud SQL

**Flechas punteadas amarillas (Secrets/Deploy):**
- Servicios → Secret Manager
- Cloud Build → Container Registry → Cloud Run

**Flecha punteada gris (SMTP):**
- notification-service → SMTP Server

### 3. Colores sugeridos:

- **Verde claro** (#d5e8d4): Servicios Cloud Run principales
- **Rojo claro** (#f8cecc): Workers/Procesadores
- **Morado claro** (#e1d5e7): Pub/Sub Topics
- **Naranja claro** (#ffe6cc): Cloud SQL
- **Amarillo claro** (#fff2cc): Secret Manager, Container Registry, Cloud Build
- **Gris claro** (#f5f5f5): Servicios externos

### 4. Leyenda:

Agrega una leyenda en la esquina inferior izquierda explicando:
- Líneas azules: HTTP/HTTPS
- Líneas naranjas: Pub/Sub Events
- Líneas punteadas naranjas: Database (JDBC)
- Líneas punteadas amarillas: Secrets/Deploy

## 🎨 Personalización

Una vez importado el diagrama, puedes:
- Ajustar colores según tu preferencia
- Mover componentes para mejor organización
- Agregar más detalles o anotaciones
- Exportar como PNG, PDF, SVG, etc.

## 📤 Exportar el Diagrama

1. En Draw.io, ve a **File → Export as → PNG/PDF/SVG**
2. Selecciona la calidad y opciones deseadas
3. Guarda el archivo

## 🔧 Solución de Problemas

Si el archivo XML no se importa correctamente:
1. Verifica que el archivo no esté corrupto
2. Intenta abrir Draw.io en modo incógnito
3. Usa la Opción 3 para crear el diagrama manualmente
4. Revisa la consola del navegador para errores

## 📋 Checklist de Componentes

Asegúrate de que tu diagrama incluya:

- [ ] Cliente/Internet
- [ ] API Gateway
- [ ] 5 servicios Cloud Run principales (auth, customer, product, cart, order)
- [ ] 2 workers (payment-worker, notification-service)
- [ ] 3 Topics de Pub/Sub
- [ ] 3 Subscriptions de Pub/Sub
- [ ] Cloud SQL
- [ ] Secret Manager
- [ ] Container Registry
- [ ] Cloud Build
- [ ] SMTP Server
- [ ] Todas las conexiones entre componentes
- [ ] Leyenda explicativa

