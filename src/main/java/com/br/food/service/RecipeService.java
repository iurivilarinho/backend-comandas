package com.br.food.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Product;
import com.br.food.models.RecipeItem;
import com.br.food.repository.RecipeItemRepository;
import com.br.food.request.RecipeItemRequest;

@Service
public class RecipeService {

	private final RecipeItemRepository recipeItemRepository;
	private final ProductService productService;

	public RecipeService(RecipeItemRepository recipeItemRepository, ProductService productService) {
		this.recipeItemRepository = recipeItemRepository;
		this.productService = productService;
	}

	@Transactional(readOnly = true)
	public List<RecipeItem> findByProductId(Long productId) {
		return recipeItemRepository.findByFinalProductId(productId);
	}

	@Transactional
	public List<RecipeItem> replaceRecipe(Long productId, List<RecipeItemRequest> recipeItems) {
		Product finalProduct = productService.findById(productId);
		if (finalProduct.getType() != ProductType.FINISHED) {
			throw new DataIntegrityViolationException("Recipes can only be assigned to finished products.");
		}

		recipeItemRepository.deleteByFinalProductId(productId);
		return recipeItems.stream().map(recipeItemRequest -> {
			Product ingredientProduct = productService.findById(recipeItemRequest.getIngredientProductId());
			if (ingredientProduct.getType() != ProductType.INGREDIENT) {
				throw new DataIntegrityViolationException("Recipe ingredients must use products of type INGREDIENT.");
			}
			return recipeItemRepository.save(new RecipeItem(finalProduct, ingredientProduct, recipeItemRequest.getQuantity()));
		}).toList();
	}
}
