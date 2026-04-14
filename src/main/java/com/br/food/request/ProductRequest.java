package com.br.food.request;

import java.math.BigDecimal;

import com.br.food.enums.Types.ProductType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Product create or update payload")
public class ProductRequest {

    @NotNull(message = "Product type is required.")
    private ProductType type;

    @NotBlank(message = "Description is required.")
    @Size(min = 3, max = 100, message = "Description must have between 3 and 100 characters.")
    private String description;

    @NotBlank(message = "Code is required.")
    @Size(min = 1, max = 20, message = "Code must have between 1 and 20 characters.")
    private String code;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Menu visibility is required.")
    private Boolean visibleOnMenu;

    @NotNull(message = "Complement flag is required.")
    private Boolean complement;

    public ProductType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getVisibleOnMenu() {
        return visibleOnMenu;
    }

    public Boolean getComplement() {
        return complement;
    }
}
