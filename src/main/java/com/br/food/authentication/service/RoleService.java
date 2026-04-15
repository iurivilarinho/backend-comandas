package com.br.food.authentication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.Role;
import com.br.food.models.User;
import com.br.food.repository.RoleRepository;
import com.br.food.request.RoleRequest;
import com.br.food.service.UserService;
import com.br.food.specification.RoleSpecification;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RoleService {

	private final RoleRepository roleRepository;
	private final UserService userService;

	public RoleService(RoleRepository roleRepository, UserService userService) {
		this.roleRepository = roleRepository;
		this.userService = userService;
	}

	@Transactional
	public Role create(RoleRequest payload) {
		return roleRepository.save(new Role(payload));
	}

	@Transactional(readOnly = true)
	public Role findById(Long id) {
		return roleRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Perfil nÃ£o encontrado para ID: " + id));
	}

	@Transactional(readOnly = true)
	public Role findByName(String name) {
		return roleRepository.findByNameIgnoreCase(name)
				.orElseThrow(() -> new EntityNotFoundException("Perfil nÃ£o encontrado para nome: " + name));
	}

	@Transactional(readOnly = true)
	public List<Role> findAll(Boolean status) {
		return roleRepository.findAll(RoleSpecification.active(status));
	}

	@Transactional
	public Role update(RoleRequest form, Long id) {
		Role role = findById(id);
		role.setName(form.getName());
		role.setDescription(form.getDescription());

		return roleRepository.save(role);
	}

	@Transactional
	public void enableDisable(Long idCargo, Boolean status) {
		Role role = findById(idCargo);
		role.setActive(status);
		roleRepository.save(role);
	}

	@Transactional
	public void linkRoleWithUser(Long userId, Long roleId) {
		Role role = findById(roleId);
		User user = userService.findById(userId);
		user.getRoles().add(role);
	}

	@Transactional
	public void unlinkRoleWithUser(Long userId, Long roleId) {
		User user = userService.findById(userId);
		user.getRoles().removeIf(r -> r.getId().equals(roleId));
	}
}
