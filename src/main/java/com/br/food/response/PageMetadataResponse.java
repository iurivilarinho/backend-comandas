package com.br.food.response;

import org.springframework.data.domain.Page;

public class PageMetadataResponse {

	private final int number;
	private final int size;
	private final long totalElements;
	private final int totalPages;
	private final boolean first;
	private final boolean last;

	public PageMetadataResponse(Page<?> page) {
		this.number = page.getNumber();
		this.size = page.getSize();
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.first = page.isFirst();
		this.last = page.isLast();
	}

	public int getNumber() {
		return number;
	}

	public int getSize() {
		return size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public boolean isFirst() {
		return first;
	}

	public boolean isLast() {
		return last;
	}
}
