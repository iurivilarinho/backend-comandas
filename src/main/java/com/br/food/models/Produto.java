package com.br.food.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Tipos.TipoProduto;
import com.br.food.forms.ProdutoForm;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbProduto")
public class Produto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TipoProduto tipo;

	@Column(length = 500)
	private String descricao;

	@Column(length = 6)
	private String codigo;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_Id_Documento", foreignKey = @ForeignKey(name = "FK_FROM_TBDOCUMENTO_FOR_TBPRODUTO"))
	private Documento imagem;

	@Column(nullable = false)
	private Boolean status;

	@Column(nullable = false)
	private Boolean complemento;

	@Column(nullable = false)
	private Boolean exibeCardapio;

	@Column(nullable = false)
	private BigDecimal valor;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "tbProduto_X_Complemento", joinColumns = @JoinColumn(name = "fk_Id_Produto"), foreignKey = @ForeignKey(name = "FK_FROM_TBPRODUTO_FOR_TBPRODUTO-COMPLEMENTO"), inverseJoinColumns = @JoinColumn(name = "fk_Id_ProdutoComplemento"), inverseForeignKey = @ForeignKey(name = "FK_FROMTBPRODUTO-COMPLEMENTO_FOR_TBPRODUTO"))
	private List<Produto> complementos = new ArrayList<>();

	public Produto() {

	}

	public Produto(ProdutoForm form, Documento imagem) {

		this.tipo = form.getTipo();
		this.descricao = form.getDescricao();
		this.codigo = form.getCodigo();
		this.imagem = imagem;
		this.status = true;
		this.valor = form.getValor();
	}

	public TipoProduto getTipo() {
		return tipo;
	}

	public void setTipo(TipoProduto tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Documento getImagem() {
		return imagem;
	}

	public void setImagem(Documento imagem) {
		this.imagem = imagem;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Long getId() {
		return id;
	}

	public Boolean getExibeCardapio() {
		return exibeCardapio;
	}

	public void setExibeCardapio(Boolean exibeCardapio) {
		this.exibeCardapio = exibeCardapio;
	}

	public Boolean getComplemento() {
		return complemento;
	}

	public void setComplemento(Boolean complemento) {
		this.complemento = complemento;
	}

	public List<Produto> getComplementos() {
		return complementos;
	}

	public void setComplementos(List<Produto> complementos) {
		this.complementos = complementos;
	}

}