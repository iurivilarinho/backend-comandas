package com.br.food.dto;

import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Status.StatusItem;
import com.br.food.enums.Status.StatusPedido;
import com.br.food.enums.Tipos.TipoPedido;
import com.br.food.models.Cliente;
import com.br.food.models.ItemPedido;
import com.br.food.models.Mesa;
import com.br.food.models.Pedido;

import lombok.Data;

@Data
public class PedidoItemPendenteDTO {
	private Long id;
	private Cliente cliente;
	private Mesa mesa;
	private List<ItemPedido> itens = new ArrayList<>();
	private String codigo;
	private StatusPedido status;
	private TipoPedido tipo;

	public PedidoItemPendenteDTO(Pedido pedido) {
		this.id = pedido.getId();
		this.cliente = pedido.getCliente();
		this.mesa = pedido.getMesa();
		this.itens = pedido.getItens().stream().filter(i -> i.getStatus().equals(StatusItem.PENDENTE)).toList();
		this.codigo = pedido.getCodigo();
		this.status = pedido.getStatus();
		this.tipo = pedido.getTipo();
	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public Mesa getMesa() {
		return mesa;
	}

	public List<ItemPedido> getItens() {
		return itens;
	}

	public String getCodigo() {
		return codigo;
	}

	public StatusPedido getStatus() {
		return status;
	}

	public TipoPedido getTipo() {
		return tipo;
	}

}
