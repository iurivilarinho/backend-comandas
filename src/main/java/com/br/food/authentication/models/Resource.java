package com.br.food.authentication.models;

import com.br.food.authentication.enums.ResourceType;
import com.br.food.authentication.request.ResourceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Schema(description = "Recurso do sistema vinculado a uma tela, utilizado no controle de permissÃµes.")
@Entity
@Table(name = "resources")
public class Resource {

	@Schema(description = "Identificador Ãºnico do recurso.", example = "1")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Schema(description = "Tipo do recurso.")
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ResourceType type;

	@Schema(description = "ReferÃªncia do componente no front-end.", example = "users.list.table")
	@Column(name = "componenteReference")
	private String componentReference;

	@Schema(description = "TÃ­tulo do recurso.", example = "Listar usuÃ¡rios")
	@Column(name = "title")
	private String title;

	@Schema(description = "Status do recurso (ativo/inativo).", example = "true")
	@Column(name = "status")
	private Boolean active;

	@Schema(description = "DescriÃ§Ã£o do recurso.", example = "Permite visualizar a listagem de usuÃ¡rios.")
	@Column(name = "description")
	private String description;

	protected Resource() {
		// JPA
	}

	public Resource(ResourceRequest form) {
		this.title = form.title();
		this.description = form.description();
		this.componentReference = form.componentReference();
		this.type = form.type();
		this.active = Boolean.TRUE;
	}

	@PrePersist
	private void prePersist() {
		if (this.active == null) {
			this.active = Boolean.TRUE;
		}
	}

	public Long getId() {
		return id;
	}

	public ResourceType getType() {
		return type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public String getComponentReference() {
		return componentReference;
	}

	public void setComponentReference(String componentReference) {
		this.componentReference = componentReference;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
