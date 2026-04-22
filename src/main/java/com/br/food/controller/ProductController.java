package com.br.food.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.models.Product;
import com.br.food.enums.Types.ProductType;
import com.br.food.request.ProductRequest;
import com.br.food.request.RecipeItemRequest;
import com.br.food.response.ProductResponse;
import com.br.food.response.RecipeItemResponse;
import com.br.food.service.ProductService;
import com.br.food.service.RecipeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/products")
@Tag(name = "Products", description = "Endpoints for product management")
public class ProductController {

	private final ProductService productService;
	private final RecipeService recipeService;

	public ProductController(ProductService productService, RecipeService recipeService) {
		this.productService = productService;
		this.recipeService = recipeService;
	}

	@Operation(summary = "Create product")
	@ApiResponse(responseCode = "201", description = "Product created successfully")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> create(
			@Valid @RequestPart("request") ProductRequest request,
			@RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
		Product product = productService.create(request, image);
		return ResponseEntity.status(201).body(new ProductResponse(product));
	}

	@Operation(summary = "Find product by id")
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new ProductResponse(productService.findById(id)));
	}

	@Operation(summary = "List products")
	@GetMapping
	public ResponseEntity<Page<ProductResponse>> findAll(Pageable pageable,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Boolean visibleOnMenu,
			@RequestParam(required = false) ProductType type,
			@RequestParam(required = false) Boolean complement,
			@RequestParam(required = false) String term) {
		if (isMenuRequest(active, visibleOnMenu, type, complement)) {
			return ResponseEntity.ok(productService.findMenuProducts(pageable, categoryId, term));
		}
		return ResponseEntity.ok(productService.findAll(pageable, categoryId, active, visibleOnMenu, type, complement, term)
				.map(ProductResponse::new));
	}

	@Operation(summary = "Update product")
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> update(
			@PathVariable Long id,
			@Valid @RequestPart("request") ProductRequest request,
			@RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
		return ResponseEntity.ok(new ProductResponse(productService.update(id, request, image)));
	}

	@Operation(summary = "Update product status")
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
		productService.updateStatus(id, active);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Link complement to product")
	@PostMapping("/{productId}/complements/{complementId}")
	public ResponseEntity<Void> addComplement(@PathVariable Long productId, @PathVariable Long complementId) {
		productService.addComplement(productId, complementId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove product complement")
	@DeleteMapping("/{productId}/complements/{complementId}")
	public ResponseEntity<Void> removeComplement(@PathVariable Long productId, @PathVariable Long complementId) {
		productService.removeComplement(productId, complementId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Replace product recipe")
	@PutMapping("/{productId}/recipe")
	public ResponseEntity<List<RecipeItemResponse>> replaceRecipe(
			@PathVariable Long productId,
			@Valid @RequestBody List<RecipeItemRequest> recipeItems) {
		return ResponseEntity.ok(recipeService.replaceRecipe(productId, recipeItems).stream().map(RecipeItemResponse::new).toList());
	}

	@Operation(summary = "Delete product")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private boolean isMenuRequest(Boolean active, Boolean visibleOnMenu, ProductType type, Boolean complement) {
		return Boolean.TRUE.equals(active)
				&& Boolean.TRUE.equals(visibleOnMenu)
				&& type == ProductType.FINISHED
				&& Boolean.FALSE.equals(complement);
	}
}

