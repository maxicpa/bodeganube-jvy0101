package cl.bodeganube.picking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient ordenesRestClient(RestClient.Builder builder,
                                 @Value("${app.ordenes.url}") String url,
                                 @Value("${app.internal-key}") String clave) {
        return construir(builder, url, clave);
    }

    @Bean
    RestClient inventarioRestClient(RestClient.Builder builder,
                                    @Value("${app.inventario.url}") String url,
                                    @Value("${app.internal-key}") String clave) {
        return construir(builder, url, clave);
    }

    private RestClient construir(RestClient.Builder builder, String url, String clave) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(5000);
        return builder.requestFactory(fabrica)
                .baseUrl(url)
                .defaultHeader(InternalKeyHeader.HEADER, clave)
                .build();
    }
}
