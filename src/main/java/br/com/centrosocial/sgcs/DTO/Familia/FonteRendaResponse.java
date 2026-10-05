package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.TipoRendaBeneficio;
import java.math.BigDecimal;

public record FonteRendaResponse(TipoRendaBeneficio tipo, boolean ativa, BigDecimal valor) {}
