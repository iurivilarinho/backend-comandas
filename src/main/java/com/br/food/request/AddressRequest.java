package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Address payload")
public class AddressRequest {

	@NotBlank(message = "Street is required.")
	@Size(max = 100, message = "Street must have at most 100 characters.")
	private String street;

	@NotBlank(message = "Number is required.")
	@Size(max = 10, message = "Number must have at most 10 characters.")
	private String number;

	@NotBlank(message = "District is required.")
	@Size(max = 50, message = "District must have at most 50 characters.")
	private String district;

	@NotBlank(message = "Postal code is required.")
	@Size(max = 8, message = "Postal code must have at most 8 characters.")
	private String postalCode;

	@NotBlank(message = "City is required.")
	@Size(max = 50, message = "City must have at most 50 characters.")
	private String city;

	public String getStreet() {
		return street != null ? street.trim() : null;
	}

	public String getNumber() {
		return number != null ? number.trim() : null;
	}

	public String getDistrict() {
		return district != null ? district.trim() : null;
	}

	public String getPostalCode() {
		return postalCode != null ? postalCode.trim() : null;
	}

	public String getCity() {
		return city != null ? city.trim() : null;
	}
}
