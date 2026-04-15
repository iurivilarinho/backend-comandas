package com.br.food.response;

import java.util.Base64;

import com.br.food.models.Document;

public class DocumentResponse {

	private final Long id;
	private final String name;
	private final String contentType;
	private final Long size;
	private final String document;

	public DocumentResponse(Document document) {
		this.id = document != null ? document.getId() : null;
		this.name = document != null ? document.getName() : null;
		this.contentType = document != null ? document.getContentType() : null;
		this.size = document != null ? document.getSize() : null;
		this.document = document != null && document.getDocument() != null
				? Base64.getEncoder().encodeToString(document.getDocument())
				: null;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getContentType() {
		return contentType;
	}

	public Long getSize() {
		return size;
	}

	public String getDocument() {
		return document;
	}
}
