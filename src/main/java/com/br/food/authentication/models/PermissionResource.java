package com.br.food.authentication.models;

import com.br.food.authentication.request.PermissionRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Schema(description = "PermissÃµes de um role para um recurso (CRUD).")
@Entity
@Table(name = "permission_resources", uniqueConstraints = @UniqueConstraint(columnNames = { "fk_Id_Role",
		"fk_Id_Resource" }))
public class PermissionResource {

	@Schema(description = "Identificador Ãºnico do registro de permissÃ£o.", example = "1")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Schema(description = "Recurso ao qual as permissÃµes se aplicam.")
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "fk_Id_Resource", foreignKey = @ForeignKey(name = "FK_FROM_TBRESOURCE_FOR_TBPERMISSIONRESOURCE"))
	private Resource resource;

	@Schema(description = "Role ao qual as permissÃµes pertencem.")
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "fk_Id_Role", foreignKey = @ForeignKey(name = "FK_FROM_TBROLE_FOR_TBPERMISSIONRESOURCE"))
	private Role role;

	@Schema(description = "PermissÃ£o para criar.", example = "true")
	private Boolean canCreate;

	@Schema(description = "PermissÃ£o para ler.", example = "true")
	private Boolean canRead;

	@Schema(description = "PermissÃ£o para atualizar.", example = "true")
	private Boolean canUpdate;

	@Schema(description = "PermissÃ£o para excluir.", example = "false")
	private Boolean canDelete;

	protected PermissionResource() {
		// JPA
	}

	public PermissionResource(PermissionRequest payload, Role role, Resource resource) {
		this.canCreate = payload.canCreate();
		this.canRead = payload.canRead();
		this.canUpdate = payload.canUpdate();
		this.canDelete = payload.canDelete();
		this.role = role;
		this.resource = resource;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getCanCreate() {
		return canCreate;
	}

	public void setCanCreate(Boolean canCreate) {
		this.canCreate = canCreate;
	}

	public Boolean getCanRead() {
		return canRead;
	}

	public void setCanRead(Boolean canRead) {
		this.canRead = canRead;
	}

	public Boolean getCanUpdate() {
		return canUpdate;
	}

	public void setCanUpdate(Boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public Boolean getCanDelete() {
		return canDelete;
	}

	public void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}
}
