package com.br.food.models;

import com.br.food.request.CustomerRequest;

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
@Table(name = "tbCustomer")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String nome;

	@Column(length = 11, nullable = false, unique = true)
	private String cpf;

	@Column(length = 15, nullable = false)
	private String telefone;

	@Column(nullable = false)
	private Boolean bloqueado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Address", foreignKey = @ForeignKey(name = "FK_FROM_TBENDERECO_FOR_TBCLIENTE"))
	private Address endereco;

	public Customer() {
	}

	public Customer(CustomerRequest request) {
		this.bloqueado = false;
		this.nome = request.getName();
		this.cpf = request.getDocumentNumber();
		this.telefone = request.getPhone();
	}

	public void update(CustomerRequest request) {
		this.nome = request.getName();
		this.cpf = request.getDocumentNumber();
		this.telefone = request.getPhone();
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getName() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public String getDocumentNumber() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getPhone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public Address getAddress() {
		return endereco;
	}

	public void setAddress(Address endereco) {
		this.endereco = endereco;
	}

	public Boolean getBloqueado() {
		return bloqueado;
	}

	public Boolean getBlocked() {
		return bloqueado;
	}

	public void setBloqueado(Boolean bloqueado) {
		this.bloqueado = bloqueado;
	}

	public void setBlocked(Boolean blocked) {
		this.bloqueado = blocked;
	}

}
