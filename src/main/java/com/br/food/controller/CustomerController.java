package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.models.Customer;
import com.br.food.request.CustomerIdentifyRequest;
import com.br.food.request.CustomerRequest;
import com.br.food.response.CustomerResponse;
import com.br.food.service.ClientRateLimiter;
import com.br.food.service.CustomerAccessTokenService;
import com.br.food.service.CustomerService;
import com.br.food.service.PortalAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Endpoints for customer management")
public class CustomerController {

	private final CustomerService customerService;
	private final CustomerAccessTokenService customerAccessTokenService;
	private final PortalAccessService portalAccessService;
	private final ClientRateLimiter clientRateLimiter;

	public CustomerController(CustomerService customerService, CustomerAccessTokenService customerAccessTokenService,
			PortalAccessService portalAccessService, ClientRateLimiter clientRateLimiter) {
		this.customerService = customerService;
		this.customerAccessTokenService = customerAccessTokenService;
		this.portalAccessService = portalAccessService;
		this.clientRateLimiter = clientRateLimiter;
	}

	@Operation(summary = "Create customer", description = "Público (cadastro pelo cardápio digital). A resposta traz o token do cliente.")
	@PostMapping
	public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
		Customer customer = customerService.create(request);
		return ResponseEntity.status(201)
				.body(new CustomerResponse(customer, customerAccessTokenService.generate(customer.getId())));
	}

	@Operation(summary = "Identify customer", description = "Público. Recupera o próprio cadastro com telefone e CPF que casam; devolve o token do cliente. Qualquer outra combinação responde 404. No máximo 10 tentativas por minuto por endereço (429).")
	@PostMapping("/identify")
	public ResponseEntity<CustomerResponse> identify(@Valid @RequestBody CustomerIdentifyRequest request,
			HttpServletRequest http) {
		clientRateLimiter.check("identify", http);
		return customerService.identify(request.getPhone(), request.getDocumentNumber())
				.map(customer -> ResponseEntity
						.ok(new CustomerResponse(customer, customerAccessTokenService.generate(customer.getId()))))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Find customer by id", description = "Equipe, ou o próprio cliente com X-Customer-Token.")
	@GetMapping("/{id}")
	public ResponseEntity<CustomerResponse> findById(@PathVariable Long id,
			@RequestHeader(name = PortalAccessService.CUSTOMER_TOKEN_HEADER, required = false) String customerToken) {
		portalAccessService.requireStaffOrCustomer(customerToken, id);
		return ResponseEntity.ok(new CustomerResponse(customerService.findById(id)));
	}

	@Operation(summary = "Find customer by CPF")
	@GetMapping("/by-document")
	public ResponseEntity<CustomerResponse> findByDocumentNumber(@RequestParam String documentNumber) {
		return customerService.findByDocumentNumber(documentNumber)
				.map(customer -> ResponseEntity.ok(new CustomerResponse(customer)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Find customer by phone")
	@GetMapping("/by-phone")
	public ResponseEntity<CustomerResponse> findByPhone(@RequestParam String phone) {
		return customerService.findByPhone(phone)
				.map(customer -> ResponseEntity.ok(new CustomerResponse(customer)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "List customers")
	@GetMapping
	public ResponseEntity<Page<CustomerResponse>> findAll(Pageable pageable) {
		return ResponseEntity.ok(customerService.findAll(pageable).map(CustomerResponse::new));
	}

	@Operation(summary = "Update customer", description = "Equipe, ou o próprio cliente com X-Customer-Token.")
	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request,
			@RequestHeader(name = PortalAccessService.CUSTOMER_TOKEN_HEADER, required = false) String customerToken) {
		portalAccessService.requireStaffOrCustomer(customerToken, id);
		return ResponseEntity.ok(new CustomerResponse(customerService.update(id, request)));
	}

	@Operation(summary = "Update customer blocked status")
	@PatchMapping("/{id}/blocked")
	public ResponseEntity<Void> updateBlocked(@PathVariable Long id, @RequestParam Boolean blocked) {
		customerService.updateBlockedStatus(id, blocked);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Delete customer")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		customerService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

