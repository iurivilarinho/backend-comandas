package com.br.food.request;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Company profile create or update payload")
public class CompanyProfileRequest {

	public static final String DEFAULT_PRIMARY_COLOR = "#EA1D2C";

	@NotBlank(message = "Company name is required.")
	@Size(min = 2, max = 120, message = "Company name must have between 2 and 120 characters.")
	private String companyName;

	@Size(max = 255, message = "Slogan must have at most 255 characters.")
	private String slogan;

	@Schema(description = "Primary brand color in hexadecimal format.", example = "#EA1D2C")
	@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hexadecimal color.")
	private String primaryColor = DEFAULT_PRIMARY_COLOR;

	@NotNull(message = "Dine-in setting is required.")
	private Boolean dineInEnabled;

	@NotNull(message = "Delivery setting is required.")
	private Boolean deliveryEnabled;

	@NotNull(message = "Takeaway setting is required.")
	private Boolean takeawayEnabled;

	@Valid
	@NotNull(message = "Address is required.")
	private AddressRequest address;

	@Valid
	private List<OpeningHourRequest> openingHours = new ArrayList<>();

	public String getCompanyName() {
		return companyName != null ? companyName.trim() : null;
	}

	public String getSlogan() {
		return slogan != null && !slogan.isBlank() ? slogan.trim() : null;
	}

	public String getPrimaryColor() {
		return primaryColor != null && !primaryColor.isBlank()
				? primaryColor.trim().toUpperCase()
				: DEFAULT_PRIMARY_COLOR;
	}

	public Boolean getDineInEnabled() {
		return dineInEnabled;
	}

	public Boolean getDeliveryEnabled() {
		return deliveryEnabled;
	}

	public Boolean getTakeawayEnabled() {
		return takeawayEnabled;
	}

	public AddressRequest getAddress() {
		return address;
	}

	public List<OpeningHourRequest> getOpeningHours() {
		return openingHours != null ? openingHours : List.of();
	}
}
