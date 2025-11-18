package com.farmatodo.payment.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Configuración de timeouts para manejar cold starts y evitar "Broken pipe"
        // Basado en: https://stackoverflow.com/questions/2309561/how-to-fix-java-net-socketexception-broken-pipe
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(15))  // Tiempo para establecer conexión
                .setResponseTimeout(Timeout.ofSeconds(60)) // Tiempo para recibir respuesta
                .setConnectionRequestTimeout(Timeout.ofSeconds(10)) // Tiempo para obtener conexión del pool
                .build();
        
        // Connection pooling para reutilizar conexiones y evitar "Broken pipe"
        // Pool más grande para manejar múltiples requests concurrentes
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)  // Máximo de conexiones totales
                .setMaxConnPerRoute(20) // Máximo de conexiones por ruta (host:port)
                .build();
        
        // Configurar keep-alive y validación de conexiones
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                // Evitar conexiones inactivas que pueden causar "Broken pipe"
                .evictIdleConnections(Timeout.ofSeconds(20))  // Cerrar conexiones inactivas después de 20s
                .evictExpiredConnections()  // Cerrar conexiones expiradas
                // Validar conexiones antes de usarlas (evita usar conexiones cerradas)
                .setConnectionManagerShared(false)  // No compartir el connection manager
                .build();
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // Timeout adicional para operaciones de lectura/escritura
        factory.setConnectTimeout(15000);
        factory.setConnectionRequestTimeout(10000);
        
        return new RestTemplate(factory);
    }
}

