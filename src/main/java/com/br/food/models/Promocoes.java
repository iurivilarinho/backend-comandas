package com.br.food.models;

import com.br.food.enums.Tipos.TipoPromocao;

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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbPromocoes")
public class Promocoes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TipoPromocao tipo;

	@Column(length = 500, nullable = false)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Documento", foreignKey = @ForeignKey(name = "FK_FROM_TBDOCUMENTO_FOR_TBPROMOCOES"))
	private Documento imagem;

	@Column(nullable = false)
	private Boolean status;

	public Long getId() {
		return id;
	}

	public TipoPromocao getTipo() {
		return tipo;
	}

	public void setTipo(TipoPromocao tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
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

}