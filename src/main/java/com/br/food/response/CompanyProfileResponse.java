package com.br.food.response;

import java.util.List;

import com.br.food.models.CompanyProfile;

public class CompanyProfileResponse {

	private final Long id;
	private final String companyName;
	private final String slogan;
	private final Boolean dineInEnabled;
	private final Boolean deliveryEnabled;
	private final Boolean takeawayEnabled;
	private final DocumentResponse logo;
	private final DocumentResponse banner;
	private final AddressResponse address;
	private final List<OpeningHourResponse> openingHours;

	public CompanyProfileResponse(CompanyProfile companyProfile) {
		this.id = companyProfile.getId();
		this.companyName = companyProfile.getCompanyName();
		this.slogan = companyProfile.getSlogan();
		this.dineInEnabled = companyProfile.getDineInEnabled();
		this.deliveryEnabled = companyProfile.getDeliveryEnabled();
		this.takeawayEnabled = companyProfile.getTakeawayEnabled();
		this.logo = companyProfile.getLogo() != null ? new DocumentResponse(companyProfile.getLogo()) : null;
		this.banner = companyProfile.getBanner() != null ? new DocumentResponse(companyProfile.getBanner()) : null;
		this.address = companyProfile.getAddress() != null ? new AddressResponse(companyProfile.getAddress()) : null;
		this.openingHours = companyProfile.getOpeningHours().stream().map(OpeningHourResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getSlogan() {
		return slogan;
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

	public DocumentResponse getLogo() {
		return logo;
	}

	public DocumentResponse getBanner() {
		return banner;
	}

	public AddressResponse getAddress() {
		return address;
	}

	public List<OpeningHourResponse> getOpeningHours() {
		return openingHours;
	}
}
