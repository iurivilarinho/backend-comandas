package com.br.food.models;

import com.br.food.enums.Types.PromotionType;

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
import jakarta.persistence.Table;
@Entity
@Table(name = "tbPromotion")
public class Promotion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private PromotionType tipo;

	@Column(length = 500, nullable = false)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Document", foreignKey = @ForeignKey(name = "FK_FROM_TBDOCUMENTO_FOR_TBPROMOCOES"))
	private Document imagem;

	@Column(nullable = false)
	private Boolean status;

	public Promotion() {
	}

	public Long getId() {
		return id;
	}

	public PromotionType getTipo() {
		return tipo;
	}

	public PromotionType getType() {
		return tipo;
	}

	public void setTipo(PromotionType tipo) {
		this.tipo = tipo;
	}

	public void setType(PromotionType type) {
		this.tipo = type;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getDescription() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public void setDescription(String description) {
		this.descricao = description;
	}

	public Document getImagem() {
		return imagem;
	}

	public Document getImage() {
		return imagem;
	}

	public void setImagem(Document imagem) {
		this.imagem = imagem;
	}

	public void setImage(Document image) {
		this.imagem = image;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

}
