package com.br.food.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.forms.ClienteForm;
import com.br.food.models.Cliente;
import com.br.food.repository.ClienteRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Transactional
	public Cliente cadastrarCliente(ClienteForm form) {
		return clienteRepository.save(new Cliente(form));
	}

	@Transactional
	public Cliente editarCliente(Long id, ClienteForm form) {
		Cliente cliente = buscarClientePorId(id);

		cliente.setNome(form.getNome());
		cliente.setCpf(form.getCpf());
		cliente.setTelefone(form.getTelefone());

		return clienteRepository.save(cliente);
	}

	@Transactional(readOnly = true)
	public Cliente buscarClientePorId(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado para o ID " + id));
	}

	@Transactional(readOnly = true)
	public Page<Cliente> listarTodosClientes(Pageable pageable) {
		return clienteRepository.findAll(pageable);
	}

	@Transactional
	public void inativarAtivarCliente(Long id, Boolean bloqueado) {
		Cliente cliente = buscarClientePorId(id);
		cliente.setBloqueado(bloqueado);
		clienteRepository.save(cliente);
	}
}