package com.br.food.models;

import com.br.food.request.AddressRequest;
import com.br.food.request.CustomerRequest;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Column(name = "document_number", length = 14, nullable = false, unique = true)
	private String documentNumber;

	@Column(name = "phone", length = 15, nullable = false)
	private String phone;

	@Column(name = "blocked", nullable = false)
	private Boolean blocked;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "fk_address_id", foreignKey = @ForeignKey(name = "fk_customer_address"))
	private Address address;

	public Customer() {
	}

	public Customer(CustomerRequest request) {
		this.blocked = false;
		this.name = request.getName();
		this.documentNumber = request.getDocumentNumber();
		this.phone = request.getPhone();
		applyAddress(request.getAddress());
	}

	public void update(CustomerRequest request) {
		this.name = request.getName();
		this.documentNumber = request.getDocumentNumber();
		this.phone = request.getPhone();
		applyAddress(request.getAddress());
	}

	private void applyAddress(AddressRequest addressRequest) {
		if (addressRequest == null) {
			return;
		}
		Address target = this.address != null ? this.address : new Address();
		target.setStreet(addressRequest.getStreet());
		target.setNumber(addressRequest.getNumber());
		target.setDistrict(addressRequest.getDistrict());
		target.setPostalCode(addressRequest.getPostalCode());
		target.setCity(addressRequest.getCity());
		if (target.getStatus() == null) {
			target.setStatus(true);
		}
		this.address = target;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNome() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNome(String name) {
		this.name = name;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getCpf() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public void setCpf(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getPhone() {
		return phone;
	}

	public String getTelefone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setTelefone(String phone) {
		this.phone = phone;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public Boolean getBloqueado() {
		return blocked;
	}

	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

	public void setBloqueado(Boolean blocked) {
		this.blocked = blocked;
	}

	public Address getAddress() {
		return address;
	}

	public Address getEndereco() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
