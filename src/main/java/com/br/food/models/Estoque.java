package com.br.food.models;

import java.math.BigDecimal;

import com.br.food.forms.EstoqueForm;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbEstoque")
public class Estoque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "fk_Id_Produto", foreignKey = @ForeignKey(name = "FK_FROM_TBPRODUTO_FOR_TBESTOQUE"))
	private Produto produto;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_NotaFiscal", foreignKey = @ForeignKey(name = "FK_FROM_TBNOTAFISCAL_FOR_TBESTOQUE"))
	private NotaFiscal notaFiscal;

	@Column(length = 20, nullable = false)
	private String lote;

	@Column(nullable = false)
	private BigDecimal quantidadeDisponivel;

	@Column(nullable = false)
	private BigDecimal quantidadeReservada;

	@Column(nullable = false)
	private BigDecimal quantidadeVendida;

	@Column(nullable = false)
	private BigDecimal quantidadeEntrada;

	public Estoque(EstoqueForm form, Produto produto, NotaFiscal notaFiscal) {

		this.produto = produto;
		this.lote = form.getLote();
		this.quantidadeDisponivel = form.getQuantidade();
		this.quantidadeEntrada = form.getQuantidade();
		this.quantidadeReservada = BigDecimal.ZERO;
		this.quantidadeVendida = BigDecimal.ZERO;
		this.notaFiscal = notaFiscal;

	}

	public Estoque() {
	}

	public Long getId() {
		return id;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public NotaFiscal getNotaFiscal() {
		return notaFiscal;
	}

	public void setNotaFiscal(NotaFiscal notaFiscal) {
		this.notaFiscal = notaFiscal;
	}

	public String getLote() {
		return lote;
	}

	public void setLote(String lote) {
		this.lote = lote;
	}

	public BigDecimal getQuantidadeDisponivel() {
		return quantidadeDisponivel;
	}

	public void setQuantidadeDisponivel(BigDecimal quantidadeDisponivel) {
		this.quantidadeDisponivel = quantidadeDisponivel;
	}

	public BigDecimal getQuantidadeReservada() {
		return quantidadeReservada;
	}

	public void setQuantidadeReservada(BigDecimal quantidadeReservada) {
		this.quantidadeReservada = quantidadeReservada;
	}

	public BigDecimal getQuantidadeVendida() {
		return quantidadeVendida;
	}

	public void setQuantidadeVendida(BigDecimal quantidadeVendida) {
		this.quantidadeVendida = quantidadeVendida;
	}

	public BigDecimal getQuantidadeEntrada() {
		return quantidadeEntrada;
	}

	public void setQuantidadeEntrada(BigDecimal quantidadeEntrada) {
		this.quantidadeEntrada = quantidadeEntrada;
	}
}
