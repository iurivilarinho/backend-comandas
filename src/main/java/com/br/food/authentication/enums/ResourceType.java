package com.br.food.authentication.enums;

public enum ResourceType {
	SCREEN("Tela"), HTML_COMPONENTE("Componente HTML"), MENU("Menu");

	private String description;

	private ResourceType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
