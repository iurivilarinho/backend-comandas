package com.br.food.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_profiles")
public class CompanyProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "company_name", nullable = false, length = 120)
	private String companyName;

	@Column(name = "slogan", length = 255)
	private String slogan;

	@Column(name = "primary_color", length = 7)
	private String primaryColor;

	@Column(name = "digital_ordering_enabled")
	private Boolean digitalOrderingEnabled;

	@Column(name = "dine_in_enabled", nullable = false)
	private Boolean dineInEnabled;

	@Column(name = "delivery_enabled", nullable = false)
	private Boolean deliveryEnabled;

	@Column(name = "takeaway_enabled", nullable = false)
	private Boolean takeawayEnabled;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_logo_document_id", foreignKey = @ForeignKey(name = "fk_company_profile_logo_document"))
	private Document logo;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_banner_document_id",
			foreignKey = @ForeignKey(name = "fk_company_profile_banner_document"))
	private Document banner;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "fk_address_id", foreignKey = @ForeignKey(name = "fk_company_profile_address"))
	private Address address;

	@OneToMany(mappedBy = "companyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompanyOpeningHour> openingHours = new ArrayList<>();

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public CompanyProfile() {
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public Boolean getDigitalOrderingEnabled() {
		return digitalOrderingEnabled;
	}

	public void setDigitalOrderingEnabled(Boolean digitalOrderingEnabled) {
		this.digitalOrderingEnabled = digitalOrderingEnabled;
	}

	public Boolean getDineInEnabled() {
		return dineInEnabled;
	}

	public void setDineInEnabled(Boolean dineInEnabled) {
		this.dineInEnabled = dineInEnabled;
	}

	public Boolean getDeliveryEnabled() {
		return deliveryEnabled;
	}

	public void setDeliveryEnabled(Boolean deliveryEnabled) {
		this.deliveryEnabled = deliveryEnabled;
	}

	public Boolean getTakeawayEnabled() {
		return takeawayEnabled;
	}

	public void setTakeawayEnabled(Boolean takeawayEnabled) {
		this.takeawayEnabled = takeawayEnabled;
	}

	public Document getLogo() {
		return logo;
	}

	public void setLogo(Document logo) {
		this.logo = logo;
	}

	public Document getBanner() {
		return banner;
	}

	public void setBanner(Document banner) {
		this.banner = banner;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<CompanyOpeningHour> getOpeningHours() {
		return openingHours;
	}
}
