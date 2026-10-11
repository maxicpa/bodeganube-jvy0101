package cl.bodeganube.auth.config;

import cl.bodeganube.auth.model.Comercio;
import cl.bodeganube.auth.model.Rol;
import cl.bodeganube.auth.model.Usuario;
import cl.bodeganube.auth.repository.ComercioRepository;
import cl.bodeganube.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Datos de demostración: un operario, un comercio y un usuario de comercio. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner semilla(UsuarioRepository usuarios, ComercioRepository comercios, PasswordEncoder encoder) {
        return args -> {
            if (usuarios.count() > 0) {
                return;
            }
            Comercio demo = new Comercio();
            demo.setNombre("Tienda Demo");
            demo.setRut("76.123.456-7");
            comercios.save(demo);

            usuarios.save(usuario("Operario Bodega", "operario@bodeganube.cl", "Operario123", Rol.OPERARIO, null, encoder));
            usuarios.save(usuario("Tienda Demo", "tienda@demo.cl", "Comercio123", Rol.COMERCIO, demo, encoder));
        };
    }

    private static Usuario usuario(String nombre, String email, String clave, Rol rol, Comercio comercio,
                                   PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(clave));
        u.setRol(rol);
        u.setComercio(comercio);
        return u;
    }
}
