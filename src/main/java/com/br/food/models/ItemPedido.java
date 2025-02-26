package com.br.food.models;

import java.time.LocalDateTime;

import com.br.food.enums.Status.StatusItem;

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

	@Column(nullable = false)
	private LocalDateTime horaSolicitacao = LocalDateTime.now();

	@Enumerated(EnumType.STRING)
	private StatusItem status;

	private String motivoRecusa;

	public ItemPedido(Pedido pedido, Evento evento, Integer quantidade) {
		this.status = StatusItem.ATENDIDO;
		this.pedido = pedido;
		this.evento = evento;
		this.quantidade = quantidade;
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

	public LocalDateTime getHoraSolicitacao() {
		return horaSolicitacao;
	}

	public void setHoraSolicitacao(LocalDateTime horaSolicitacao) {
		this.horaSolicitacao = horaSolicitacao;
	}

	public StatusItem getStatus() {
		return status;
	}

	public void setStatus(StatusItem status) {
		this.status = status;
	}

	public String getMotivoRecusa() {
		return motivoRecusa;
	}

	public void setMotivoRecusa(String motivoRecusa) {
		this.motivoRecusa = motivoRecusa;
	}

}