package com.br.food.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Status.StatusNotaFiscal;
import com.br.food.forms.NotaFiscalForm;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tbNotaFiscal", uniqueConstraints = @UniqueConstraint(columnNames = { "numeroNota", "numeroSerie",
		"chaveNFE" }))
public class NotaFiscal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 20, nullable = false)
	private String numeroNota;

	@Column(length = 20, nullable = false)
	private String numeroSerie;

	@Column(length = 44, nullable = false)
	private String chaveNFE;

	@Column(nullable = false)
	private LocalDate dataEmissao;

	@OneToMany(mappedBy = "notaFiscal", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Estoque> itens = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private StatusNotaFiscal status;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_Id_Anexo", foreignKey = @ForeignKey(name = "FK_FROM_TBDOCUMENTO_FOR_TBNOTAFISCAL"))
	private Documento anexo;

	public NotaFiscal() {

	}

	public NotaFiscal(NotaFiscalForm form, Documento anexo) {

		this.numeroNota = form.getNumeroNota();
		this.numeroSerie = form.getNumeroSerie();
		this.chaveNFE = form.getChaveNFE();
		this.dataEmissao = form.getDataEmissao();
		this.anexo = anexo;
	}

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

	public StatusNotaFiscal getStatus() {
		return status;
	}

	public void setStatus(StatusNotaFiscal status) {
		this.status = status;
	}

	public Documento getAnexo() {
		return anexo;
	}

	public void setAnexo(Documento anexo) {
		this.anexo = anexo;
	}

}
