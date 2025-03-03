package com.br.food.forms;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EstoqueForm {

	private Long idProduto;
	private BigDecimal quantidade;
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
