package com.br.food.integration;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

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
import com.br.food.models.Address;
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
@AutoConfigureMockMvc(addFilters = false)
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
				.andExpect(jsonPath("$.totalAmount").value(closeTo(50.0, 0.001)))
				.andExpect(jsonPath("$.changeAmount").value(closeTo(5.0, 0.001)));

		Order closedOrder = orderRepository.findById(orderId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());
	}

	@Test
	void cancelItemShouldRestoreStockAndAdjustPaidAmount() throws Exception {
		Customer customerEntity = buildCustomer("Customer Two", "12345678902");
		customerEntity.setAddress(buildAddress());
		Customer customer = customerRepository.save(customerEntity);
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
						  "paymentMethod": "PIX",
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

	@Test
	void retainedStockShouldBeIgnoredDuringRecipeConsumption() throws Exception {
		Customer customer = customerRepository.save(buildCustomer("Customer Three", "12345678903"));
		DiningTable table = diningTableRepository.save(new DiningTable("12"));

		Product ingredient = productRepository.save(buildProduct("ING-3", "Cheese", ProductType.INGREDIENT, new BigDecimal("1.00")));
		Product finishedProduct = productRepository.save(buildProduct("FIN-3", "Toast", ProductType.FINISHED, new BigDecimal("12.00")));
		recipeItemRepository.save(new RecipeItem(finishedProduct, ingredient, new BigDecimal("1.000")));

		StockEntry retainedEntry = buildStockEntry(ingredient, new BigDecimal("5.000"), "BATCH-3A");
		retainedEntry.setRetained(true);
		stockEntryRepository.save(retainedEntry);
		stockEntryRepository.save(buildStockEntry(ingredient, new BigDecimal("3.000"), "BATCH-3B"));

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
						      "notes": ""
						    }
						  ]
						}
						""".formatted(customer.getId(), table.getNumber(), finishedProduct.getId())))
				.andExpect(status().isCreated())
				.andReturn();

		Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("items").get(0).get("id").asLong();

		mockMvc.perform(post("/kitchen/accept/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/kitchen/start-preparation/{itemId}", itemId).header("X-Actor", "cook"))
				.andExpect(status().isNoContent());

		List<StockEntry> entries = stockEntryRepository.findAll();
		StockEntry stillRetained = entries.stream().filter(StockEntry::isRetained).findFirst().orElseThrow();
		StockEntry consumedReleased = entries.stream().filter(entry -> !entry.isRetained()).findFirst().orElseThrow();

		org.junit.jupiter.api.Assertions.assertEquals(0, stillRetained.getAvailableQuantity().compareTo(new BigDecimal("5.000")));
		org.junit.jupiter.api.Assertions.assertEquals(0, consumedReleased.getAvailableQuantity().compareTo(new BigDecimal("1.000")));
	}

	@Test
	void updateRetentionEndpointShouldToggleStockEntryStatus() throws Exception {
		Product ingredient = productRepository.save(buildProduct("ING-4", "Sauce", ProductType.INGREDIENT, new BigDecimal("1.00")));
		StockEntry stockEntry = stockEntryRepository.save(buildStockEntry(ingredient, new BigDecimal("2.000"), "BATCH-4"));

		mockMvc.perform(patch("/stock/{id}/retention", stockEntry.getId()).param("retained", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.retained", is(true)));

		StockEntry updatedStockEntry = stockEntryRepository.findById(stockEntry.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(true, updatedStockEntry.isRetained());
	}

	@Test
	void itemOutsideKitchenShouldStartReadyAndStayOutOfKitchenQueue() throws Exception {
		Customer customer = customerRepository.save(buildCustomer("Customer Four", "12345678904"));
		DiningTable table = diningTableRepository.save(new DiningTable("13"));

		Product finishedProduct = buildProduct("FIN-4", "Bottled Juice", ProductType.FINISHED, new BigDecimal("9.50"));
		finishedProduct.setSendToKitchen(false);
		finishedProduct.setRequiresPreparation(false);
		finishedProduct = productRepository.save(finishedProduct);

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
						      "quantity": 1,
						      "notes": "Serve cold"
						    }
						  ]
						}
						""".formatted(customer.getId(), table.getNumber(), finishedProduct.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items[0].status").value("READY"))
				.andReturn();

		Long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
		Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("items").get(0).get("id").asLong();

		mockMvc.perform(get("/kitchen/pending"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@.id == %s)]".formatted(orderId)).doesNotExist());

		mockMvc.perform(post("/orders/{orderId}/items/{itemId}/serve", orderId, itemId)
				.header("X-Actor", "waiter"))
				.andExpect(status().isNoContent());

		Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.READY_TO_CLOSE, updatedOrder.getStatus());

		mockMvc.perform(get("/orders/{orderId}", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("SERVED"));
	}

	@Test
	void financialReportEndpointShouldReturnExcelFile() throws Exception {
		mockMvc.perform(post("/financial/entries")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Actor", "cashier")
				.content("""
						{
						  "type": "EXPENSE",
						  "category": "OPERATIONS",
						  "description": "Internet bill",
						  "amount": 150.00,
						  "paymentMethod": "PIX"
						}
						"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/financial/report"))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Type", containsString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
				.andExpect(header().string("Content-Disposition", containsString(".xlsx")));
	}

	private Customer buildCustomer(String name, String documentNumber) {
		Customer customer = new Customer();
		customer.setName(name);
		customer.setDocumentNumber(documentNumber);
		customer.setPhone("11999999999");
		customer.setBlocked(false);
		return customer;
	}

	private Address buildAddress() {
		Address address = new Address();
		address.setStreet("Rua Teste");
		address.setNumber("100");
		address.setDistrict("Centro");
		address.setPostalCode("01001000");
		address.setCity("Sao Paulo");
		address.setStatus(true);
		return address;
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
		product.setSendToKitchen(type == ProductType.FINISHED);
		product.setRequiresPreparation(type == ProductType.FINISHED);
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
		stockEntry.setRetained(false);
		return stockEntry;
	}
}
