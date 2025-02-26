package com.br.food.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbCliente")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String nome;

	@Column(length = 11, nullable = false, unique = true)
	private String cpf;

	@Column(length = 15, nullable = false)
	private String telefone;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Endereco", foreignKey = @ForeignKey(name = "FK_FROM_TBENDERECO_FOR_TBCLIENTE"))
	private Endereco endereco;

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
}