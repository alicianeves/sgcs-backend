package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.TipoRendaBeneficio;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record FonteRendaRequest(@NotNull TipoRendaBeneficio tipo, boolean ativa, @PositiveOrZero BigDecimal valor) {}
