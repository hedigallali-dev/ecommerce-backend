package com.ecommerce.exposition.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CONFIGURATION SWAGGER / OPENAPI 3
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI cartOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Panier E-commerce")
                        .description("""
                                Backend de gestion du panier pour une plateforme e-commerce multi-utilisateurs.
                                
                                **Architecture :** Hexagonale (Ports & Adapters)
                                
                                **Produits de test disponibles :**
                                | productId | label | offerId | état | prix | remise | stock |
                                |---|---|---|---|---|---|---|
                                | P001 | iPhone 15 Pro | O001 | NEUF | 1199€ | 0% | 10 |
                                | P001 | iPhone 15 Pro | O002 | RECONDITIONNE | 1199€ | 15% | 5 |
                                | P001 | iPhone 15 Pro | O003 | OCCASION | 1199€ | 30% | 0 ← rupture |
                                | P002 | MacBook Air M3 | O004 | NEUF | 1499€ | 0% | 8 |
                                | P002 | MacBook Air M3 | O005 | RECONDITIONNE | 1499€ | 20% | 3 |
                                | P003 | AirPods Pro 2 | O006 | NEUF | 279€ | 10% | 20 |
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Serveur local")
                ));
    }
}
