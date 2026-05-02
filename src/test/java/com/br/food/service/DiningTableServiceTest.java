//package com.br.food.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import com.br.food.models.DiningTable;
//import com.br.food.repository.DiningTableRepository;
//import com.br.food.repository.OrderRepository;
//
//@ExtendWith(MockitoExtension.class)
//class DiningTableServiceTest {
//
//	@Mock
//	private DiningTableRepository diningTableRepository;
//
//	@Mock
//	private OrderRepository orderRepository;
//
//	@Mock
//	private TableAccessTokenService tableAccessTokenService;
//
//	@InjectMocks
//	private DiningTableService diningTableService;
//
//	@Test
//	void findAllShouldHideInactiveTablesByDefaultAndSortActiveTablesByNumber() {
//		DiningTable tableTen = createTable(10L, "10", true, false);
//		DiningTable tableTwo = createTable(2L, "2", true, false);
//		DiningTable inactiveTable = createTable(20L, "20", false, false);
//
//		when(diningTableRepository.findAll()).thenReturn(List.of(tableTen, tableTwo, inactiveTable));
//		when(orderRepository.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
//
//		List<com.br.food.response.DiningTableResponse> response = diningTableService.findAll(null);
//
//		assertEquals(List.of("2", "10"), response.stream().map(com.br.food.response.DiningTableResponse::getNumber).toList());
//	}
//
//	@Test
//	void findAllShouldReturnInactiveTablesWhenFilteringByInactive() {
//		DiningTable activeTable = createTable(1L, "1", true, false);
//		DiningTable inactiveTable = createTable(20L, "20", false, false);
//
//		when(diningTableRepository.findAll()).thenReturn(List.of(activeTable, inactiveTable));
//		when(orderRepository.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
//
//		List<com.br.food.response.DiningTableResponse> response = diningTableService.findAll("INACTIVE");
//
//		assertEquals(1, response.size());
//		assertEquals("20", response.get(0).getNumber());
//	}
//
//	private DiningTable createTable(Long id, String number, boolean active, boolean occupied) {
//		DiningTable table = new DiningTable(number);
//		ReflectionTestUtils.setField(table, "id", id);
//		table.setActive(active);
//		table.setOccupied(occupied);
//		return table;
//	}
//}
