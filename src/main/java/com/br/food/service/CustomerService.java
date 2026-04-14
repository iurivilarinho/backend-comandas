package com.br.food.service;

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
		return customerRepository.save(new Customer(request));
	}

	@Transactional
	public Customer update(Long id, CustomerRequest request) {
		Customer customer = findById(id);
		customer.update(request);
		return customerRepository.save(customer);
	}

	@Transactional(readOnly = true)
	public Customer findById(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Customer not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Customer> findAll(Pageable pageable) {
		return customerRepository.findAll(pageable);
	}

	@Transactional
	public void updateBlockedStatus(Long id, Boolean blocked) {
		Customer customer = findById(id);
		customer.setBlocked(blocked);
	}
}
