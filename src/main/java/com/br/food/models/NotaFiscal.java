package com.br.food.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbNotaFiscal")
public class NotaFiscal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 20, nullable = false, unique = true)
	private String numeroNota;

	@Column(length = 20, nullable = false)
	private String numeroSerie;

	@Column(length = 44, nullable = false, unique = true)
	private String chaveNFE;

	@Column(nullable = false)
	private LocalDate dataEmissao;

	@OneToMany(mappedBy = "notaFiscal", fetch = FetchType.LAZY)
	private List<Estoque> itens = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getNumeroNota() {
		return numeroNota;
	}

	public void setNumeroNota(String numeroNota) {
		this.numeroNota = numeroNota;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public void setNumeroSerie(String numeroSerie) {
		this.numeroSerie = numeroSerie;
	}

	public String getChaveNFE() {
		return chaveNFE;
	}

	public void setChaveNFE(String chaveNFE) {
		this.chaveNFE = chaveNFE;
	}

	public LocalDate getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(LocalDate dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	public List<Estoque> getItens() {
		return itens;
	}

	public void setItens(List<Estoque> itens) {
		this.itens = itens;
	}

}
