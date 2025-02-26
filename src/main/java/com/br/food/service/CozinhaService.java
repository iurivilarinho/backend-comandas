package com.br.food.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.dto.PedidoItemPendenteDTO;
import com.br.food.enums.Status.StatusItem;
import com.br.food.enums.Tipos.TipoProduto;
import com.br.food.models.ItemPedido;
import com.br.food.repository.filtros.PedidoFiltro;

@Service
public class CozinhaService {

	@Autowired
	private PedidoFiltro pedidoFiltro;

	@Autowired
	private ItemPedidoService itemPedidoService;

	public void aceitarItemPedido(Long idItemPedido) {
		ItemPedido item = itemPedidoService.buscarItemPorId(idItemPedido);
		validaStatusItem(item);

		itemPedidoService.alterarStatusItem(idItemPedido,
				item.getProduto().getTipo().equals(TipoProduto.ACABADO) ? StatusItem.ATENDIDO : StatusItem.EM_PREPARO);
	}

	public void recusarItem(Long idItemPedido, String motivoRecusa) {
		ItemPedido item = itemPedidoService.buscarItemPorId(idItemPedido);
		validaStatusItem(item);
		itemPedidoService.adicionarMotivoRecusaItem(idItemPedido, motivoRecusa);
		itemPedidoService.alterarStatusItem(idItemPedido, StatusItem.RECUSADO);
	}

	public void validaStatusItem(ItemPedido item) {
		switch (item.getStatus()) {
		case ATENDIDO:
			throw new DataIntegrityViolationException("Este item já foi atendido para este pedido.");

		case RECUSADO:
			throw new DataIntegrityViolationException("Este item foi recusado.");

		default:
			break;
		}
	}

	@Transactional(readOnly = true)
	public Page<PedidoItemPendenteDTO> exibePedidoItensPendentes(Pageable page) {
		return pedidoFiltro.filtro(true, page).map(PedidoItemPendenteDTO::new);
	}

}
