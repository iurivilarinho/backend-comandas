package com.br.food.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.ProductCategory;
import com.br.food.repository.ProductCategoryRepository;
import com.br.food.request.ProductCategoryRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductCategoryService {

	private final ProductCategoryRepository productCategoryRepository;

	public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
		this.productCategoryRepository = productCategoryRepository;
	}

	@Transactional(readOnly = true)
	public ProductCategory findById(Long id) {
		return productCategoryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product category not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public List<ProductCategory> findAll(Boolean active) {
		if (active == null) {
			return productCategoryRepository.findAllByOrderByNameAsc();
		}
		return productCategoryRepository.findAllByActiveOrderByNameAsc(active);
	}

	@Transactional
	public ProductCategory create(ProductCategoryRequest request) {
		validateUniqueName(request.getName(), null);
		return productCategoryRepository.save(new ProductCategory(request));
	}

	@Transactional
	public ProductCategory update(Long id, ProductCategoryRequest request) {
		ProductCategory category = findById(id);
		validateUniqueName(request.getName(), id);
		category.update(request);
		return productCategoryRepository.save(category);
	}

	@Transactional
	public void updateStatus(Long id, boolean active) {
		ProductCategory category = findById(id);
		category.setActive(active);
	}

	private void validateUniqueName(String name, Long currentId) {
		productCategoryRepository.findByNameIgnoreCase(name)
				.filter(category -> currentId == null || !category.getId().equals(currentId))
				.ifPresent(category -> {
					throw new DataIntegrityViolationException(
							"There is already a product category using name " + name + ".");
				});
	}
}
