package com.br.food.response;

import com.br.food.models.Customer;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cliente do estabelecimento")
public class CustomerResponse {

	private final Long id;
	private final String name;
	private final String documentNumber;
	private final String phone;
	private final Boolean blocked;
	private final AddressResponse address;

	@Schema(description = "Token do cliente para o cardápio digital, enviado em X-Customer-Token. So vem no cadastro e na identificação.")
	private final String accessToken;

	public CustomerResponse(Customer customer) {
		this(customer, null);
	}

	public CustomerResponse(Customer customer, String accessToken) {
		this.accessToken = accessToken;
		this.id = customer.getId();
		this.name = customer.getName();
		this.documentNumber = customer.getDocumentNumber();
		this.phone = customer.getPhone();
		this.blocked = customer.getBlocked();
		this.address = customer.getAddress() != null ? new AddressResponse(customer.getAddress()) : null;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getPhone() {
		return phone;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public AddressResponse getAddress() {
		return address;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getAccessToken() {
		return accessToken;
	}
}
