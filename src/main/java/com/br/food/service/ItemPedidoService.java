package com.br.food.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Status.StatusItem;
import com.br.food.models.ItemPedido;
import com.br.food.repository.ItemPedidoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItemPedidoService {

	@Autowired
	private ItemPedidoRepository itemPedidoRepository;

	@Transactional(readOnly = true)
	public ItemPedido buscarItemPorId(Long id) {
		return itemPedidoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Item não encontrado para ID " + id));
	}

	@Transactional
	public void adicionarMotivoRecusaItem(Long id, String motivoRecusa) {
		ItemPedido item = buscarItemPorId(id);
		item.setMotivoRecusa(motivoRecusa);

		itemPedidoRepository.save(item);
	}

	@Transactional
	public void alterarStatusItem(Long idItem, StatusItem status) {
		ItemPedido item = buscarItemPorId(idItem);
		item.setStatus(status);
		itemPedidoRepository.save(item);
	}

}
