package com.br.food.response;

import com.br.food.models.Address;

public class AddressResponse {

	private final Long id;
	private final String street;
	private final String number;
	private final String district;
	private final String postalCode;
	private final String city;

	public AddressResponse(Address address) {
		this.id = address.getId();
		this.street = address.getStreet();
		this.number = address.getNumber();
		this.district = address.getDistrict();
		this.postalCode = address.getPostalCode();
		this.city = address.getCity();
	}

	public Long getId() {
		return id;
	}

	public String getStreet() {
		return street;
	}

	public String getNumber() {
		return number;
	}

	public String getDistrict() {
		return district;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCity() {
		return city;
	}
}
