package cl.bodeganube.ordenes.util;

public final class Texto {

    private Texto() {
    }

    public static String truncar(String texto, int max) {
        if (texto == null || texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max);
    }
}
