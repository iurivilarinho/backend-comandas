package com.br.food.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbEstoque")
public class Estoque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Produto", foreignKey = @ForeignKey(name = "FK_FROM_TBPRODUTO_FOR_TBESTOQUE"))
	private Produto produto;

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
