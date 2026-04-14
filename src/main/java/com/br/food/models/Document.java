package com.br.food.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbDocument")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;

	private String contentType;

	private Long tamanho;

	@Lob
	private byte[] documento;

	public Document() {

	}

	public Document(String nome, String contentType, byte[] documento) {

		this.nome = nome;
		this.contentType = contentType;
		this.tamanho = documento != null ? (long) documento.length : null;
		this.documento = documento;
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

	public void setName(String name) {
		this.nome = name;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getTamanho() {
		return tamanho;
	}

	public Long getSize() {
		return tamanho;
	}

	public void setTamanho(Long tamanho) {
		this.tamanho = tamanho;
	}

	public void setSize(Long size) {
		this.tamanho = size;
	}

	public byte[] getDocument() {
		return documento;
	}

	public void setDocument(byte[] documento) {
		this.documento = documento;
	}

	public Long getId() {
		return id;
	}

}
