package com.br.food.forms;

import java.math.BigDecimal;

import com.br.food.enums.Tipos.TipoProduto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProdutoForm {

	@NotNull(message = "O tipo do produto é obrigatório")
	private TipoProduto tipo;

	@NotBlank(message = "A descrição é obrigatória")
	@Size(min = 3, max = 100, message = "A descrição deve ter entre 3 e 100 caracteres")
	private String descricao;

	@NotBlank(message = "O código é obrigatório")
	@Size(min = 1, max = 20, message = "O código deve ter entre 1 e 20 caracteres")
	private String codigo;

	@NotNull(message = "O valor é obrigatório")
	@DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
	private BigDecimal valor;

	public TipoProduto getTipo() {
		return tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getCodigo() {
		return codigo;
	}

	public BigDecimal getValor() {
		return valor;
	}
}