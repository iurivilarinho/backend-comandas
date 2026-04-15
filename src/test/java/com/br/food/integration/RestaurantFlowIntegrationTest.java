package com.br.food.integration;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.Customer;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.models.Product;
import com.br.food.models.RecipeItem;
import com.br.food.models.StockEntry;
import com.br.food.repository.CustomerRepository;
import com.br.food.repository.DiningTableRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.RecipeItemRepository;
import com.br.food.repository.StockEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private DiningTableRepository diningTableRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private RecipeItemRepository recipeItemRepository;

	@Autowired
	private StockEntryRepository stockEntryRepository;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		recipeItemRepository.deleteAll();
		stockEntryRepository.deleteAll();
		productRepository.deleteAll();
		customerRepository.deleteAll();
		diningTableRepository.deleteAll();
	}

	@Test
	void kitchenFlowShouldConsumeStockAndCloseOrderWithMultiplePayments() throws Exception {
		Customer customer = customerRepository.save(buildCustomer("Customer One", "12345678901"));
		DiningTable table = diningTableRepository.save(new DiningTable("10"));

		Product ingredient = productRepository.save(buildProduct("ING-1", "Bread", ProductType.INGREDIENT, new BigDecimal("1.00")));
		Product finishedProduct = productRepository.save(buildProduct("FIN-1", "Burger", ProductType.FINISHED, new BigDecimal("25.00")));
		recipeItemRepository.save(new RecipeItem(finishedProduct, ingredient, new BigDecimal("1.000")));
		stockEntryRepository.save(buildStockEntry(ingredient, new BigDecimal("10.000"), "BATCH-1"));

		MvcResult createResult = mockMvc.perform(post("/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "waiter")
				.content("""
						{
						  "customerId": %d,
						  "tableNumber": "%s",
						  "channel": "DINE_IN",
						  "discountPercentage": 0,
						  "items": [
						    {
						      "productId": %d,
						      "quantity": 2,
						      "notes": "No onion"
						    }
						  ]
						}
						""".formatted(customer.getId(), table.getNumber(), finishedProduct.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andReturn();

		JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
		Long orderId = createJson.get("id").asLong();
		Long itemId = createJson.get("items").get(0).get("id").asLong();

		mockMvc.perform(post("/kitchen/accept/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/kitchen/start-preparation/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());

		StockEntry updatedStock = stockEntryRepository.findAll().get(0);
		org.junit.jupiter.api.Assertions.assertEquals(0, updatedStock.getAvailableQuantity().compareTo(new BigDecimal("8.000")));

		mockMvc.perform(post("/kitchen/mark-ready/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/orders/{orderId}/close", orderId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "cashier")
				.content("""
						{
						  "splitByPersonCount": 2,
						  "payments": [
						    {
						      "paymentMethod": "CARD",
						      "amount": 20.00
						    },
						    {
						      "paymentMethod": "CASH",
						      "amount": 40.00,
						      "cashReceived": 45.00
						    }
						  ]
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.closed").value(true))
				.andExpect(jsonPath("$.totalAmount").value(closeTo(60.0, 0.001)))
				.andExpect(jsonPath("$.changeAmount").value(closeTo(5.0, 0.001)))
				.andExpect(jsonPath("$.amountPerPerson").value(closeTo(30.0, 0.001)));

		Order closedOrder = orderRepository.findById(orderId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());
	}

	@Test
	void cancelItemShouldRestoreStockAndAdjustPaidAmount() throws Exception {
		Customer customer = customerRepository.save(buildCustomer("Customer Two", "12345678902"));
		DiningTable table = diningTableRepository.save(new DiningTable("11"));

		Product ingredient = productRepository.save(buildProduct("ING-2", "Dough", ProductType.INGREDIENT, new BigDecimal("1.00")));
		Product finishedProduct = productRepository.save(buildProduct("FIN-2", "Pizza", ProductType.FINISHED, new BigDecimal("50.00")));
		recipeItemRepository.save(new RecipeItem(finishedProduct, ingredient, new BigDecimal("1.000")));
		stockEntryRepository.save(buildStockEntry(ingredient, new BigDecimal("4.000"), "BATCH-2"));

		MvcResult createResult = mockMvc.perform(post("/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "waiter")
				.content("""
						{
						  "customerId": %d,
						  "tableNumber": "%s",
						  "channel": "DELIVERY",
						  "discountPercentage": 0,
						  "items": [
						    {
						      "productId": %d,
						      "quantity": 1,
						      "notes": ""
						    }
						  ]
						}
						""".formatted(customer.getId(), table.getNumber(), finishedProduct.getId())))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
		Long orderId = createJson.get("id").asLong();
		Long itemId = createJson.get("items").get(0).get("id").asLong();

		mockMvc.perform(post("/kitchen/accept/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/kitchen/start-preparation/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/kitchen/mark-ready/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/orders/{orderId}/close", orderId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "cashier")
				.content("""
						{
						  "payments": [
						    {
						      "paymentMethod": "CARD",
						      "amount": 20.00
						    }
						  ]
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.closed").value(false))
				.andExpect(jsonPath("$.remainingAmount").value(closeTo(30.0, 0.001)));

		mockMvc.perform(post("/orders/{orderId}/items/{itemId}/cancel", orderId, itemId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "manager")
				.content("""
						{
						  "reason": "Customer changed mind"
						}
						"""))
				.andExpect(status().isNoContent());

		Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
		StockEntry restoredStock = stockEntryRepository.findAll().get(0);

		org.junit.jupiter.api.Assertions.assertEquals(0, restoredStock.getAvailableQuantity().compareTo(new BigDecimal("4.000")));
		org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("0.00"), updatedOrder.getPaidAmount());
		org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("0.00"), updatedOrder.getTotalAmount());
	}

	private Customer buildCustomer(String name, String documentNumber) {
		Customer customer = new Customer();
		customer.setName(name);
		customer.setDocumentNumber(documentNumber);
		customer.setPhone("11999999999");
		customer.setBlocked(false);
		return customer;
	}

	private Product buildProduct(String code, String description, ProductType type, BigDecimal price) {
		Product product = new Product();
		product.setCode(code);
		product.setDescription(description);
		product.setType(type);
		product.setPrice(price);
		product.setActive(true);
		product.setComplement(false);
		product.setVisibleOnMenu(true);
		return product;
	}

	private StockEntry buildStockEntry(Product product, BigDecimal quantity, String batch) {
		StockEntry stockEntry = new StockEntry();
		stockEntry.setProduct(product);
		stockEntry.setBatch(batch);
		stockEntry.setAvailableQuantity(quantity);
		stockEntry.setInputQuantity(quantity);
		stockEntry.setReservedQuantity(BigDecimal.ZERO);
		stockEntry.setSoldQuantity(BigDecimal.ZERO);
		return stockEntry;
	}
}
