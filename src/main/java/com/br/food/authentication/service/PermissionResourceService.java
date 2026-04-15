package com.br.food.authentication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.PermissionResource;
import com.br.food.authentication.models.Resource;
import com.br.food.authentication.models.Role;
import com.br.food.authentication.repository.PermissionResourceRepository;
import com.br.food.authentication.request.PermissionRequest;
import com.br.food.authentication.specification.PermissionResourceSpecification;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PermissionResourceService {

	private final PermissionResourceRepository permissionRepository;
	private final RoleService roleService;
	private final ResourceService resourceService;

	public PermissionResourceService(PermissionResourceRepository permissionRepository, RoleService roleService,
			ResourceService resourceService) {
		this.permissionRepository = permissionRepository;
		this.roleService = roleService;
		this.resourceService = resourceService;
	}

	@Transactional(readOnly = true)
	public List<PermissionResource> findAll(Long resourceId) {
		return permissionRepository.findAll(PermissionResourceSpecification.resourceIdEquals(resourceId));
	}

	@Transactional(readOnly = true)
	public PermissionResource findById(Long id) {
		return permissionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("PermissÃ£o nÃ£o encontrada para ID " + id));
	}

	@Transactional
	public PermissionResource create(PermissionRequest payload) {
		Role role = roleService.findById(payload.roleId());
		Resource resource = resourceService.findById(payload.resourceId());

		PermissionResource permission = new PermissionResource(payload, role, resource);
		return permissionRepository.save(permission);
	}

	@Transactional
	public PermissionResource update(Long id, PermissionRequest payload) {
		PermissionResource permission = findById(id);

		permission.setCanCreate(payload.canCreate());
		permission.setCanRead(payload.canRead());
		permission.setCanUpdate(payload.canUpdate());
		permission.setCanDelete(payload.canDelete());

		if (payload.roleId() != null
				&& (permission.getRole() == null || !payload.roleId().equals(permission.getRole().getId()))) {
			permission.setRole(roleService.findById(payload.roleId()));
		}

		if (payload.resourceId() != null && (permission.getResource() == null
				|| !payload.resourceId().equals(permission.getResource().getId()))) {
			permission.setResource(resourceService.findById(payload.resourceId()));
		}

		return permissionRepository.save(permission);
	}

	@Transactional
	public void delete(Long id) {
		permissionRepository.deleteById(id);
	}
}
