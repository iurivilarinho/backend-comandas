package com.br.food.forms;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NotaFiscalForm {

	@NotNull(message = "O número da nota é obrigatório")
	@Size(min = 1, max = 20, message = "O número da nota deve ter entre 1 e 20 caracteres")
	private String numeroNota;

	@NotNull(message = "O número de série é obrigatório")
	@Size(min = 1, max = 10, message = "O número de série deve ter entre 1 e 10 caracteres")
	private String numeroSerie;

	@NotNull(message = "A chave da NFE é obrigatória")
	@Size(min = 44, max = 44, message = "A chave da NFE deve ter exatamente 44 caracteres")
	@Pattern(regexp = "^[0-9]*$", message = "A chave da NFE deve conter apenas números")
	private String chaveNFE;

	@NotNull(message = "A data de emissão é obrigatória")
	private LocalDate dataEmissao;

	@NotEmpty(message = "A lista de itens não pode estar vazia")
	@Valid
	private List<EstoqueForm> itens = new ArrayList<>();

	public String getNumeroNota() {
		return numeroNota;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getChaveNFE() {
		return chaveNFE;
	}

	public LocalDate getDataEmissao() {
		return dataEmissao;
	}

	public List<EstoqueForm> getItens() {
		return itens;
	}

}