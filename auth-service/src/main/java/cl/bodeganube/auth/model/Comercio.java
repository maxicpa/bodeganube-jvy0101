package cl.bodeganube.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comercios")
@Getter
@Setter
@NoArgsConstructor
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String rut;

    /** Un comercio tiene muchos usuarios (lado inverso de Usuario.comercio). */
    @OneToMany(mappedBy = "comercio")
    private List<Usuario> usuarios = new ArrayList<>();
}
