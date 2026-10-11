package cl.bodeganube.auth.dto;

import cl.bodeganube.auth.model.Rol;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, Rol rol, Long comercioId) {
}
