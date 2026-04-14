package com.br.food.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbAddress")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String rua;

	@Column(length = 10, nullable = false)
	private String numero;

	@Column(length = 50, nullable = false)
	private String bairro;

	@Column(length = 8, nullable = false)
	private String cep;

	@Column(length = 50, nullable = false)
	private String cidade;

	@Column(nullable = false)
	private Boolean status;

	public Address() {
	}

	public Long getId() {
		return id;
	}

	public String getRua() {
		return rua;
	}

	public String getStreet() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

	public void setStreet(String street) {
		this.rua = street;
	}

	public String getNumero() {
		return numero;
	}

	public String getNumber() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public void setNumber(String number) {
		this.numero = number;
	}

	public String getBairro() {
		return bairro;
	}

	public String getDistrict() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public void setDistrict(String district) {
		this.bairro = district;
	}

	public String getCep() {
		return cep;
	}

	public String getPostalCode() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public void setPostalCode(String postalCode) {
		this.cep = postalCode;
	}

	public String getCidade() {
		return cidade;
	}

	public String getCity() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public void setCity(String city) {
		this.cidade = city;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}
}
