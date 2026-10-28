package com.br.food.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import com.br.food.authentication.token.TenantClaim;
import com.br.food.config.PlanFeatures;
import com.br.food.enums.Types.CostPriceSource;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.Document;
import com.br.food.models.Product;
import com.br.food.models.ProductCategory;
import com.br.food.models.ProductCostHistory;
import com.br.food.repository.ProductCategoryRepository;
import com.br.food.repository.ProductCostHistoryRepository;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.ProductSpecification;
import com.br.food.request.ProductRequest;
import com.br.food.response.ProductCostHistoryResponse;
import com.br.food.response.ProductResponse;

import br.com.gloomy.platform.entitlements.Entitlements;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	public static final String MENU_PRODUCTS_CACHE = "menuProducts";

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final ProductCostHistoryRepository productCostHistoryRepository;
	private final DocumentService documentService;
	private final Entitlements entitlements;

	public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository,
			ProductCostHistoryRepository productCostHistoryRepository, DocumentService documentService,
			Entitlements entitlements) {
		this.entitlements = entitlements;
		this.productRepository = productRepository;
		this.productCategoryRepository = productCategoryRepository;
		this.productCostHistoryRepository = productCostHistoryRepository;
		this.documentService = documentService;
	}

	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para o id " + id + "."));
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
		return productRepository.findAll(specification, applyDefaultSort(pageable));
	}

	private Pageable applyDefaultSort(Pageable pageable) {
		if (pageable.getSort().isSorted()) {
			return pageable;
		}
		// ignoreCase() viraria LOWER(description) no SQL, e o spec usa SELECT DISTINCT
		// quando filtra por categoria — Postgres rejeita ORDER BY de expressão fora
		// do SELECT list nesse caso. Sort puro no campo description fica compatível.
		Sort defaultSort = Sort.by(Sort.Order.asc("description"));
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
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
		entitlements.requireBelow(PlanFeatures.PRODUCTS_MAX, productRepository.count());
		validateUniqueCode(request.getCode(), null);
		validatePreparationFlags(request);
		validateVariationGroups(request);
		Document document = documentService.convertToDocument(image);
		Product product = new Product(request, document);
		product.setCategories(resolveCategories(request.getCategoryIds()));
		product.replaceVariationGroups(request.getVariationGroups());
		Product saved = productRepository.save(product);
		if (saved.getCostPrice() != null) {
			recordCostHistory(saved, saved.getCostPrice(), CostPriceSource.MANUAL, null);
		}
		return saved;
	}

	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public Product update(Long id, ProductRequest request, MultipartFile image) throws IOException {
		Product product = findById(id);
		validateUniqueCode(request.getCode(), id);
		validatePreparationFlags(request);
		validateVariationGroups(request);
		BigDecimal previousCostPrice = product.getCostPrice();
		Document document = image != null ? documentService.convertToDocument(image) : null;
		product.update(request, document);
		product.setCategories(resolveCategories(request.getCategoryIds()));
		product.replaceVariationGroups(request.getVariationGroups());
		if (request.getType() == ProductType.INGREDIENT || !request.getResolvedRequiresPreparation()) {
			product.getRecipeItems().clear();
		}
		Product saved = productRepository.save(product);
		if (costPriceChanged(previousCostPrice, saved.getCostPrice())) {
			recordCostHistory(saved, saved.getCostPrice(), CostPriceSource.MANUAL, null);
		}
		return saved;
	}

	@Transactional(readOnly = true)
	public List<ProductCostHistoryResponse> findCostHistory(Long productId) {
		findById(productId);
		return productCostHistoryRepository.findByProductIdOrderByRecordedAtDesc(productId).stream()
				.map(ProductCostHistoryResponse::new)
				.toList();
	}

	/**
	 * Aplica o custo declarado em uma nota fiscal ao produto. A NF mais recente
	 * sempre sobrescreve o custo atual; o historico so registra quando muda.
	 */
	@Transactional
	@CacheEvict(cacheNames = MENU_PRODUCTS_CACHE, allEntries = true)
	public void applyCostFromInvoice(Product product, BigDecimal unitCost, String invoiceNumber) {
		if (unitCost == null) {
			return;
		}
		BigDecimal previousCostPrice = product.getCostPrice();
		product.setCostPrice(unitCost);
		productRepository.save(product);
		if (costPriceChanged(previousCostPrice, unitCost)) {
			recordCostHistory(product, unitCost, CostPriceSource.SUPPLY_INVOICE, invoiceNumber);
		}
	}

	private void recordCostHistory(Product product, BigDecimal costPrice, CostPriceSource source, String invoiceNumber) {
		productCostHistoryRepository.save(new ProductCostHistory(product, costPrice, source, invoiceNumber));
	}

	private boolean costPriceChanged(BigDecimal previous, BigDecimal current) {
		if (current == null) {
			return false;
		}
		return previous == null || previous.compareTo(current) != 0;
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
			throw new DataIntegrityViolationException("Apenas produtos marcados como complemento podem ser vinculados.");
		}
		if (!Boolean.TRUE.equals(complement.getActive())) {
			throw new DataIntegrityViolationException("Complementos inativos não podem ser vinculados.");
		}
		if (productId.equals(complementId)) {
			throw new DataIntegrityViolationException("Um produto não pode ser complemento de si mesmo.");
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
		return TenantClaim.current() + "|" + pageNumber + "|" + pageSize + "|" + sort + "|" + categoryId + "|" + normalizedTerm;
	}

	private void validateUniqueCode(String code, Long currentId) {
		productRepository.findAll().stream().filter(product -> product.getCode().equalsIgnoreCase(code))
				.filter(product -> currentId == null || !product.getId().equals(currentId)).findFirst()
				.ifPresent(product -> {
					throw new DataIntegrityViolationException("Já existe um produto utilizando o código " + code + ".");
				});
	}

	private void validatePreparationFlags(ProductRequest request) {
		if (request.getType() != ProductType.FINISHED) {
			return;
		}
		if (request.getResolvedRequiresPreparation() && !request.getResolvedSendToKitchen()) {
			throw new DataIntegrityViolationException("Produtos que exigem preparo precisam ser enviados para a cozinha.");
		}
	}

	private void validateVariationGroups(ProductRequest request) {
		if (request.getType() != ProductType.FINISHED) {
			if (!request.getVariationGroups().isEmpty()) {
				throw new DataIntegrityViolationException("Apenas produtos finais podem ter grupos de variação.");
			}
			return;
		}

		List<String> normalizedTitles = new ArrayList<>();
		for (com.br.food.request.ProductVariationGroupRequest group : request.getVariationGroups()) {
			String normalizedTitle = group.getTitle().trim().toLowerCase();
			if (normalizedTitles.contains(normalizedTitle)) {
				throw new DataIntegrityViolationException("Os títulos dos grupos de variação precisam ser únicos por produto.");
			}
			normalizedTitles.add(normalizedTitle);

			if (group.getVariations().isEmpty()) {
				throw new DataIntegrityViolationException("O grupo de variação \"" + group.getTitle() + "\" precisa ter ao menos uma opção.");
			}

			List<String> normalizedNames = new ArrayList<>();
			for (com.br.food.request.ProductVariationRequest variation : group.getVariations()) {
				String normalizedName = variation.getName().trim().toLowerCase();
				if (normalizedNames.contains(normalizedName)) {
					throw new DataIntegrityViolationException("Os nomes das variações precisam ser únicos dentro do grupo \"" + group.getTitle() + "\".");
				}
				normalizedNames.add(normalizedName);
			}
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
			throw new DataIntegrityViolationException("Uma ou mais categorias do produto são inválidas ou estão inativas.");
		}

		return categories;
	}
}
