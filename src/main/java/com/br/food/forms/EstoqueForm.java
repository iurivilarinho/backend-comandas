package com.br.food.forms;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EstoqueForm {

	@NotNull(message = "O ID do produto é obrigatório")
	private Long idProduto;

	@NotNull(message = "A quantidade é obrigatória")
	@Positive(message = "A quantidade deve ser maior que zero")
	private BigDecimal quantidade;

	@NotNull(message = "O lote é obrigatório")
	@Size(min = 1, max = 50, message = "O lote deve ter entre 1 e 50 caracteres")
	private String lote;

	public Long getIdProduto() {
		return idProduto;
	}

	public BigDecimal getQuantidade() {
		return quantidade;
	}

	public String getLote() {
		return lote;
	}
}