package com.br.food.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FinancialStatementProjection {

	String getEntryId();

	Long getSourceId();

	String getOrigin();

	String getType();

	String getCategory();

	String getDescription();

	String getReferenceCode();

	String getNotes();

	BigDecimal getAmount();

	String getPaymentMethod();

	LocalDateTime getOccurredAt();
}
