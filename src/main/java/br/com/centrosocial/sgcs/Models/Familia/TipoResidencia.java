package br.com.centrosocial.sgcs.Models.Familia;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;
import java.util.Locale;

public enum TipoResidencia {
    PROPRIA,
    CEDIDA,
    ALUGADA;

    @JsonCreator
    public static TipoResidencia fromValue(String valor) {
        if (valor == null) return null;
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return TipoResidencia.valueOf(normalizado);
    }
}
