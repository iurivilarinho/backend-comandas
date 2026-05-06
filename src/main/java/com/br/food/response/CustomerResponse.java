package com.br.food.response;

import com.br.food.models.Customer;

public class CustomerResponse {

	private final Long id;
	private final String name;
	private final String documentNumber;
	private final String phone;
	private final Boolean blocked;
	private final AddressResponse address;

	public CustomerResponse(Customer customer) {
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
}
