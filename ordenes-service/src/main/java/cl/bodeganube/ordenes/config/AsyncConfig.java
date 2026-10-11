package cl.bodeganube.ordenes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Habilita @Async: el procesamiento del aviso corre en otro hilo, separado de la recepción. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
