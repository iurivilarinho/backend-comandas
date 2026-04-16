package com.br.food.authentication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.Resource;
import com.br.food.authentication.repository.ResourceRepository;
import com.br.food.authentication.request.ResourceRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ResourceService {

	private final ResourceRepository resourceRepository;

	public ResourceService(ResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
	}

	@Transactional(readOnly = true)
	public List<Resource> findAll() {
		return resourceRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Resource findById(Long id) {
		return resourceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Recurso nÃ£o encontrado para ID " + id));
	}

	@Transactional
	public Resource create(ResourceRequest payload) {
		Resource resource = new Resource(payload);
		return resourceRepository.save(resource);
	}

	@Transactional
	public Resource update(Long id, ResourceRequest payload) {
		Resource resource = findById(id);

		resource.setTitle(payload.title());
		resource.setDescription(payload.description());
		resource.setComponentReference(payload.componentReference());
		resource.setType(payload.type());
		resource.setActive(payload.active());

		return resourceRepository.save(resource);
	}

	@Transactional
	public void enableDisable(Long id, Boolean active) {
		Resource resource = findById(id);
		resource.setActive(active);
		resourceRepository.save(resource);
	}

	@Transactional
	public void delete(Long id) {
		resourceRepository.deleteById(id);
	}
}
