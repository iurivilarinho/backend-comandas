package com.br.food.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbItemPedido")
public class ItemPedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Produto", foreignKey = @ForeignKey(name = "FK_FROM_TBPRODUTO_FOR_TBITEMPEDIDO"))
	private Produto produto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Pedido", foreignKey = @ForeignKey(name = "FK_FROM_TBPEDIDO_FOR_TBITEMPEDIDO"))
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Evento", foreignKey = @ForeignKey(name = "FK_FROM_TBEVENTO_FOR_TBITEMPEDIDO"))
	private Evento evento;

	@Column(nullable = false)
	private Integer quantidade;

	@Column(length = 255)
	private String observacao;

	public Long getId() {
		return id;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
}