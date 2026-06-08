package com.br.food.service;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.Customer;
import com.br.food.repository.CustomerRepository;
import com.br.food.request.CustomerRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional
	public Customer create(CustomerRequest request) {
		validateUniqueDocumentNumber(request.getDocumentNumber(), null);
		return customerRepository.save(new Customer(request));
	}

	@Transactional
	public Customer update(Long id, CustomerRequest request) {
		Customer customer = findById(id);
		validateUniqueDocumentNumber(request.getDocumentNumber(), id);
		customer.update(request);
		return customerRepository.save(customer);
	}

	@Transactional(readOnly = true)
	public Customer findById(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado para o id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Customer> findAll(Pageable pageable) {
		return customerRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Customer> findByDocumentNumber(String documentNumber) {
		if (documentNumber == null || documentNumber.isBlank()) {
			return Optional.empty();
		}
		String normalizedDocumentNumber = documentNumber.replaceAll("\\D", "");
		if (normalizedDocumentNumber.isEmpty()) {
			return Optional.empty();
		}
		return customerRepository.findByDocumentNumber(normalizedDocumentNumber);
	}

	@Transactional(readOnly = true)
	public Optional<Customer> findByPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return Optional.empty();
		}
		String normalizedPhone = phone.replaceAll("\\D", "");
		if (normalizedPhone.isEmpty()) {
			return Optional.empty();
		}
		return customerRepository.findByPhone(normalizedPhone);
	}

	@Transactional
	public void updateBlockedStatus(Long id, Boolean blocked) {
		Customer customer = findById(id);
		customer.setBlocked(blocked);
	}

	@Transactional
	public void delete(Long id) {
		customerRepository.delete(findById(id));
	}

	private void validateUniqueDocumentNumber(String documentNumber, Long currentCustomerId) {
		if (documentNumber == null || documentNumber.isBlank()) {
			return;
		}
		findByDocumentNumber(documentNumber)
				.filter(customer -> currentCustomerId == null || !customer.getId().equals(currentCustomerId))
				.ifPresent(customer -> {
					throw new DataIntegrityViolationException("Ja existe um cliente utilizando este CPF.");
				});
	}
}
