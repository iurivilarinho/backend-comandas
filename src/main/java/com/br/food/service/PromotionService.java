package com.br.food.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.Product;
import com.br.food.models.Promotion;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.PromotionRepository;
import com.br.food.repository.PromotionSpecification;
import com.br.food.request.PromotionRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final ProductRepository productRepository;

	public PromotionService(PromotionRepository promotionRepository, ProductRepository productRepository) {
		this.promotionRepository = promotionRepository;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public Promotion findById(Long id) {
		return promotionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Promocao nao encontrada para o id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Promotion> findAll(Pageable pageable, Boolean active, Boolean onlyValid) {
		Specification<Promotion> specification = Specification.where(PromotionSpecification.hasActive(active))
				.and(PromotionSpecification.notExpired(onlyValid));
		return promotionRepository.findAll(specification, pageable);
	}

	@Transactional
	public Promotion create(PromotionRequest request) {
		validatePriceDisplay(request);
		Promotion promotion = new Promotion(request);
		promotion.setProducts(resolveProducts(request.getProductIds()));
		return promotionRepository.save(promotion);
	}

	@Transactional
	public Promotion update(Long id, PromotionRequest request) {
		validatePriceDisplay(request);
		Promotion promotion = findById(id);
		promotion.update(request);
		promotion.setProducts(resolveProducts(request.getProductIds()));
		return promotionRepository.save(promotion);
	}

	@Transactional
	public void updateStatus(Long id, boolean active) {
		Promotion promotion = findById(id);
		promotion.setActive(active);
	}

	@Transactional
	public void disableExpiredPromotions() {
		List<Promotion> expiredPromotions = promotionRepository.findAllByActiveTrueAndExpiresAtBefore(java.time.LocalDate.now());
		expiredPromotions.forEach(promotion -> promotion.setActive(false));
	}

	private List<Product> resolveProducts(List<Long> productIds) {
		List<Product> products = productRepository.findAllById(productIds).stream()
				.filter(product -> Boolean.TRUE.equals(product.getActive()))
				.toList();

		if (products.size() != productIds.stream().distinct().count()) {
			throw new DataIntegrityViolationException("Um ou mais produtos selecionados para a promocao sao invalidos.");
		}

		return products;
	}

	private void validatePriceDisplay(PromotionRequest request) {
		boolean hasOldPrice = request.getOldPrice() != null;
		boolean hasNewPrice = request.getNewPrice() != null;
		if (hasOldPrice != hasNewPrice) {
			throw new DataIntegrityViolationException("O preco antigo e o novo precisam ser informados juntos.");
		}
	}
}
