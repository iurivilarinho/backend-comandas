package com.br.food.forms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Tipos.TipoPagamento;
import com.br.food.enums.Tipos.TipoPedido;
import com.br.food.models.ItemPedido;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PedidoForm {

	@NotNull(message = "O ID do cliente é obrigatório")
	private Long clienteId;

	@NotNull(message = "O número da mesa é obrigatório")
	private String numeroMesa;

	@NotNull(message = "O tipo do pedido é obrigatório")
	private TipoPedido tipo;

	@Min(value = 0, message = "O percentual de desconto deve ser no mínimo 0")
	@Max(value = 100, message = "O percentual de desconto deve ser no máximo 100")
	private BigDecimal percentualDesconto;

	private TipoPagamento tipoPagamento;
	
    @Size(min = 1, message = "O pedido deve ter pelo menos um item")
	private List<ItemPedido> itens = new ArrayList<>();

	public Long getClienteId() {
		return clienteId;
	}

	public String getNumeroMesa() {
		return numeroMesa;
	}

	public TipoPedido getTipo() {
		return tipo;
	}

	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public BigDecimal getPercentualDesconto() {
		return percentualDesconto;
	}

	public List<ItemPedido> getItens() {
		return itens;
	}

}
