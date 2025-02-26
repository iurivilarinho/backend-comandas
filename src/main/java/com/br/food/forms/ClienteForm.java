package com.br.food.forms;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteForm {

	@NotBlank(message = "O nome é obrigatório")
	@Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
	private String nome;

	@NotBlank(message = "O CPF é obrigatório")
	@Size(min = 11, max = 11, message = "O CPF deve ter 11 caracteres")
	private String cpf;

	@NotBlank(message = "O telefone é obrigatório")
	@Size(min = 10, max = 15, message = "O telefone deve ter entre 10 e 15 caracteres")
	private String telefone;

	public String getNome() {
		return nome;
	}

	public String getCpf() {
		return cpf;
	}

	public String getTelefone() {
		return telefone;
	}

}