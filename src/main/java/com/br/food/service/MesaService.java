package com.br.food.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.Mesa;
import com.br.food.repository.MesaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MesaService {

	@Autowired
	private MesaRepository mesaRepository;

	@Transactional
	public List<Mesa> cadastrarMesas(Integer quantidadeMesas) {

		List<Mesa> mesas = new ArrayList<>();

		for (int i = 0; i <= quantidadeMesas; i++) {
			mesas.add(new Mesa(gerarNumeroMesa()));
		}

		return mesaRepository.saveAll(mesas);
	}

	@Transactional(readOnly = true)
	public String gerarNumeroMesa() {
		Mesa mesaMaiorNumero = mesaRepository.findTopByOrderByNumeroDesc();
		int novoNumero = mesaMaiorNumero != null ? Integer.parseInt(mesaMaiorNumero.getNumero()) + 1 : 1;

		return String.valueOf(novoNumero);
	}

	@Transactional
	public Mesa editarMesa(Long id, String numero) {
		Mesa mesa = buscarMesaPorId(id);
		mesa.setNumero(numero);
		return mesaRepository.save(mesa);
	}

	@Transactional(readOnly = true)
	public Mesa buscarMesaPorId(Long id) {
		return mesaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada para o ID " + id));
	}

	@Transactional(readOnly = true)
	public Mesa buscarMesaPorNumero(String numero) {
		return mesaRepository.findByNumero(numero)
				.orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada para o número " + numero));
	}

	@Transactional(readOnly = true)
	public List<Mesa> listarTodasMesas() {
		return mesaRepository.findAll();
	}

	@Transactional
	public void inativarAtivarMesa(Long id, Boolean status) {
		Mesa mesa = buscarMesaPorId(id);
		mesa.setStatus(status);
		mesaRepository.save(mesa);
	}

	@Transactional(readOnly = true)
	public void reservarMesa(Long id) throws AccessDeniedException {
		Mesa mesa = buscarMesaPorId(id);
		if (mesa.getOcupada()) {
			throw new AccessDeniedException("Mesa já reservada, selecione outra mesa para continuar.");
		} else {
			mesa.setOcupada(true);
		}
	}

	@Transactional(readOnly = true)
	public void liberarMesa(Long id) {
		Mesa mesa = buscarMesaPorId(id);
		mesa.setOcupada(false);
	}
}