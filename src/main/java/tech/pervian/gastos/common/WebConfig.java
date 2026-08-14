package tech.pervian.gastos.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] origensPermitidas;

    public WebConfig(@Value("${app.cors.origens}") String origens) {
        this.origensPermitidas = origens.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // lista explicita de origens, nunca "*": o frontend e conhecido
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("API de Controle de Gastos Residenciais")
                .version("v1")
                .description("""
                        Mesma API implementada antes em C#/.NET, agora em Java com Spring Boot.
                        O contrato (rotas, campos e códigos de status) é idêntico ao da versão original.

                        Ambiente de demonstração: os dados são reiniciados periodicamente.
                        """)
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
