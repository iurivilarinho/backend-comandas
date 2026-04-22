package com.br.food.response;

import com.br.food.models.Document;

public class DocumentBasicResponse {

	private final Long id;
	private final String name;
	private final String contentType;
	private final Long size;

	public DocumentBasicResponse(Document document) {
		this.id = document != null ? document.getId() : null;
		this.name = document != null ? document.getName() : null;
		this.contentType = document != null ? document.getContentType() : null;
		this.size = document != null ? document.getSize() : null;
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
}
