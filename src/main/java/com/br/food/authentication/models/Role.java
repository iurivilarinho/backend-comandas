package com.br.food.authentication.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import com.br.food.request.RoleRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {

	@Schema(description = "Identificador Ãºnico do perfil.", example = "1")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Schema(description = "Nome do perfil.", example = "ROLE_ADMIN")
	@Column(name = "nome")
	private String name;

	@Schema(description = "Decrição do perfil.", example = "ROLE_ADMIN")
	private String description;

	@Schema(description = "Status do perfil (ativo/inativo).", example = "true")
	@Column(name = "status")
	private Boolean active;

	@OneToMany(mappedBy = "role")
	private Set<PermissionResource> permissions = new HashSet<>();

	public Role() {
	}

	public Role(RoleRequest payload) {
		this.name = payload.getName();
		this.description = payload.getDescription();
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

	public String getNome() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Set<PermissionResource> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<PermissionResource> permissions) {
		this.permissions = permissions;
	}

	@Override
	public String getAuthority() {
		return name;
	}

	public void setAuthority(String authority) {
		this.description = authority;

	}
}
