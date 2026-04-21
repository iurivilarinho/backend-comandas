package com.br.food.service;

import java.io.IOException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Document;
import com.br.food.models.Product;
import com.br.food.repository.ProductRepository;
import com.br.food.request.ProductRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final DocumentService documentService;

	public ProductService(ProductRepository productRepository, DocumentService documentService) {
		this.productRepository = productRepository;
		this.documentService = documentService;
	}

	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Product> findAll(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	@Transactional
	public Product create(ProductRequest request, MultipartFile image) throws IOException {
		validateUniqueCode(request.getCode(), null);
		validatePreparationFlags(request);
		Document document = documentService.convertToDocument(image);
		return productRepository.save(new Product(request, document));
	}

	@Transactional
	public Product update(Long id, ProductRequest request, MultipartFile image) throws IOException {
		Product product = findById(id);
		validateUniqueCode(request.getCode(), id);
		validatePreparationFlags(request);
		Document document = image != null ? documentService.convertToDocument(image) : null;
		product.update(request, document);
		if (request.getType() == ProductType.INGREDIENT || !request.getResolvedRequiresPreparation()) {
			product.getRecipeItems().clear();
		}
		return productRepository.save(product);
	}

	@Transactional
	public void updateStatus(Long id, boolean active) {
		Product product = findById(id);
		product.setActive(active);
	}

	@Transactional
	public void delete(Long id) {
		Product product = findById(id);
		productRepository.delete(product);
	}

	@Transactional
	public void addComplement(Long productId, Long complementId) {
		Product product = findById(productId);
		Product complement = findById(complementId);

		if (!Boolean.TRUE.equals(complement.getComplement())) {
			throw new DataIntegrityViolationException("Only products flagged as complement can be linked.");
		}
		if (!Boolean.TRUE.equals(complement.getActive())) {
			throw new DataIntegrityViolationException("Inactive complements cannot be linked.");
		}
		if (productId.equals(complementId)) {
			throw new DataIntegrityViolationException("A product cannot complement itself.");
		}

		boolean alreadyLinked = product.getComplements().stream()
				.anyMatch(currentComplement -> currentComplement.getId().equals(complementId));
		if (!alreadyLinked) {
			product.getComplements().add(complement);
		}
	}

	@Transactional
	public void removeComplement(Long productId, Long complementId) {
		Product product = findById(productId);
		product.getComplements().removeIf(complement -> complement.getId().equals(complementId));
	}

	private void validateUniqueCode(String code, Long currentId) {
		productRepository.findAll().stream().filter(product -> product.getCode().equalsIgnoreCase(code))
				.filter(product -> currentId == null || !product.getId().equals(currentId)).findFirst()
				.ifPresent(product -> {
					throw new DataIntegrityViolationException("There is already a product using code " + code + ".");
				});
	}

	private void validatePreparationFlags(ProductRequest request) {
		if (request.getType() != ProductType.FINISHED) {
			return;
		}
		if (request.getResolvedRequiresPreparation() && !request.getResolvedSendToKitchen()) {
			throw new DataIntegrityViolationException("Products that require preparation must be sent to the kitchen.");
		}
	}
}
