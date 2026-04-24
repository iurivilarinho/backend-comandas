package com.br.food.response;

import java.util.List;

import com.br.food.models.CompanyProfile;
import com.br.food.request.CompanyProfileRequest;

public class CompanyProfileResponse {

	private final Long id;
	private final String companyName;
	private final String slogan;
	private final String primaryColor;
	private final Boolean digitalOrderingEnabled;
	private final Boolean dineInEnabled;
	private final Boolean deliveryEnabled;
	private final Boolean takeawayEnabled;
	private final DocumentBasicResponse logo;
	private final DocumentBasicResponse banner;
	private final AddressResponse address;
	private final List<OpeningHourResponse> openingHours;

	public CompanyProfileResponse(CompanyProfile companyProfile) {
		this.id = companyProfile.getId();
		this.companyName = companyProfile.getCompanyName();
		this.slogan = companyProfile.getSlogan();
		this.primaryColor = companyProfile.getPrimaryColor() != null
				? companyProfile.getPrimaryColor()
				: CompanyProfileRequest.DEFAULT_PRIMARY_COLOR;
		this.digitalOrderingEnabled = companyProfile.getDigitalOrderingEnabled() != null
				? companyProfile.getDigitalOrderingEnabled()
				: CompanyProfileRequest.DEFAULT_DIGITAL_ORDERING_ENABLED;
		this.dineInEnabled = companyProfile.getDineInEnabled();
		this.deliveryEnabled = companyProfile.getDeliveryEnabled();
		this.takeawayEnabled = companyProfile.getTakeawayEnabled();
		this.logo = companyProfile.getLogo() != null ? new DocumentBasicResponse(companyProfile.getLogo()) : null;
		this.banner = companyProfile.getBanner() != null ? new DocumentBasicResponse(companyProfile.getBanner()) : null;
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

	public String getPrimaryColor() {
		return primaryColor;
	}

	public Boolean getDigitalOrderingEnabled() {
		return digitalOrderingEnabled;
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

	public DocumentBasicResponse getLogo() {
		return logo;
	}

	public DocumentBasicResponse getBanner() {
		return banner;
	}

	public AddressResponse getAddress() {
		return address;
	}

	public List<OpeningHourResponse> getOpeningHours() {
		return openingHours;
	}
}
