package com.br.food.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Document;
import com.br.food.models.Product;
import com.br.food.models.ProductCategory;
import com.br.food.repository.ProductCategoryRepository;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.ProductSpecification;
import com.br.food.request.ProductRequest;
import com.br.food.response.ProductResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	public static final String MENU_PRODUCTS_CACHE = "menuProducts";

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final DocumentService documentService;

	public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository,
			DocumentService documentService) {
		this.productRepository = productRepository;
		this.productCategoryRepository = productCategoryRepository;
		this.documentService = documentService;
	}

	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Product> findAll(Pageable pageable, Long categoryId, Boolean active, Boolean visibleOnMenu,
			ProductType type, Boolean complement, String term) {
		Specification<Product> specification = Specification.where(ProductSpecification.hasCategoryId(categoryId))
				.and(ProductSpecification.hasActive(active))
				.and(ProductSpecification.hasVisibleOnMenu(visibleOnMenu))
				.and(ProductSpecification.hasType(type))
				.and(ProductSpecification.hasComplement(complement))
				.and(ProductSpecification.search(term));
		return productRepository.findAll(specification, pageable);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = MENU_PRODUCTS_CACHE, key = "T(com.br.food.service.ProductService).buildMenuCacheKey(#pageable, #categoryId, #term)")
	public Page<ProductResponse> findMenuProducts(Pageable pageable, Long categoryId, String term) {
		Page<Product> page = findAll(pageable, categoryId, true, true, ProductType.FINISHED, false, term);
		List<ProductResponse> content = page.getContent().stream()
				.map(ProductResponse::new)
				.toList();
		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public Product create(ProductRequest request, MultipartFile image) throws IOException {
		validateUniqueCode(request.getCode(), null);
		validatePreparationFlags(request);
		Document document = documentService.convertToDocument(image);
		Product product = new Product(request, document);
		product.setCategories(resolveCategories(request.getCategoryIds()));
		return productRepository.save(product);
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public Product update(Long id, ProductRequest request, MultipartFile image) throws IOException {
		Product product = findById(id);
		validateUniqueCode(request.getCode(), id);
		validatePreparationFlags(request);
		Document document = image != null ? documentService.convertToDocument(image) : null;
		product.update(request, document);
		product.setCategories(resolveCategories(request.getCategoryIds()));
		if (request.getType() == ProductType.INGREDIENT || !request.getResolvedRequiresPreparation()) {
			product.getRecipeItems().clear();
		}
		return productRepository.save(product);
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public void updateStatus(Long id, boolean active) {
		Product product = findById(id);
		product.setActive(active);
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public void delete(Long id) {
		Product product = findById(id);
		productRepository.delete(product);
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
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
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public void removeComplement(Long productId, Long complementId) {
		Product product = findById(productId);
		product.getComplements().removeIf(complement -> complement.getId().equals(complementId));
	}

	public static String buildMenuCacheKey(Pageable pageable, Long categoryId, String term) {
		int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
		int pageSize = pageable != null ? pageable.getPageSize() : 20;
		String sort = pageable != null ? pageable.getSort().toString() : "UNSORTED";
		String normalizedTerm = term == null ? "" : term.trim().toLowerCase();
		return pageNumber + "|" + pageSize + "|" + sort + "|" + categoryId + "|" + normalizedTerm;
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

	private List<ProductCategory> resolveCategories(List<Long> categoryIds) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			return new ArrayList<>();
		}

		List<ProductCategory> categories = productCategoryRepository.findAllById(categoryIds).stream()
				.filter(category -> Boolean.TRUE.equals(category.getActive()))
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));

		if (categories.size() != categoryIds.stream().distinct().count()) {
			throw new DataIntegrityViolationException("One or more product categories are invalid or inactive.");
		}

		return categories;
	}
}
