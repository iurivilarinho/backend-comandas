package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Types.ProductType;
import com.br.food.request.ProductRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
@Schema(description = "Product sold by the establishment")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ProductType type;

	@Column(name = "description", nullable = false, length = 500)
	private String description;

	@Column(name = "code", nullable = false, length = 20, unique = true)
	private String code;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_Id_Document", foreignKey = @ForeignKey(name = "FK_FROM_PRODUCT_FOR_DOCUMENT"))
	private Document image;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "complement", nullable = false)
	private Boolean complement;

	@Column(name = "visible_on_menu", nullable = false)
	private Boolean visibleOnMenu;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "product_complement", joinColumns = @JoinColumn(name = "fk_Id_Product"),
			inverseJoinColumns = @JoinColumn(name = "fk_Id_Complement"))
	private List<Product> complements = new ArrayList<>();

	@OneToMany(mappedBy = "finalProduct", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecipeItem> recipeItems = new ArrayList<>();

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Product() {
	}

	public Product(ProductRequest request, Document image) {
		this.type = request.getType();
		this.description = request.getDescription();
		this.code = request.getCode();
		this.image = image;
		this.active = true;
		this.complement = request.getComplement();
		this.visibleOnMenu = request.getVisibleOnMenu();
		this.price = request.getPrice();
	}

	public void update(ProductRequest request, Document image) {
		this.type = request.getType();
		this.description = request.getDescription();
		this.code = request.getCode();
		this.price = request.getPrice();
		this.complement = request.getComplement();
		this.visibleOnMenu = request.getVisibleOnMenu();
		if (image != null) {
			this.image = image;
		}
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public ProductType getType() {
		return type;
	}

	public void setType(ProductType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Document getImage() {
		return image;
	}

	public void setImage(Document image) {
		this.image = image;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getComplement() {
		return complement;
	}

	public void setComplement(Boolean complement) {
		this.complement = complement;
	}

	public Boolean getVisibleOnMenu() {
		return visibleOnMenu;
	}

	public void setVisibleOnMenu(Boolean visibleOnMenu) {
		this.visibleOnMenu = visibleOnMenu;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public List<Product> getComplements() {
		return complements;
	}

	public void setComplements(List<Product> complements) {
		this.complements = complements;
	}

	public List<RecipeItem> getRecipeItems() {
		return recipeItems;
	}
}
