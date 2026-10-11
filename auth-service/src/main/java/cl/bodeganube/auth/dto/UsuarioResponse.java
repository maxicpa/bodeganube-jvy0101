package cl.bodeganube.auth.dto;

import cl.bodeganube.auth.model.Rol;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo, Long comercioId) {
}
