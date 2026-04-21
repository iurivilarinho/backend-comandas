package com.br.food.util;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.enums.Types.FinanceOrigin;
import com.br.food.enums.Types.PaymentMethod;

public final class FinancialLabelUtils {

	private FinancialLabelUtils() {
	}

	public static String origin(FinanceOrigin origin) {
		if (origin == null) {
			return "";
		}
		return switch (origin) {
		case ORDER -> "Pedido";
		case MANUAL -> "Manual";
		};
	}

	public static String type(FinanceEntryType type) {
		if (type == null) {
			return "";
		}
		return switch (type) {
		case INCOME -> "Entrada";
		case EXPENSE -> "Saida";
		};
	}

	public static String category(FinanceCategory category) {
		if (category == null) {
			return "";
		}
		return switch (category) {
		case PRODUCTS -> "Produtos";
		case SERVICES -> "Servicos";
		case EVENTS -> "Eventos";
		case SUPPLIES -> "Insumos";
		case TAXES -> "Taxas";
		case OPERATIONS -> "Operacao";
		case OTHER -> "Outros";
		};
	}

	public static String paymentMethod(PaymentMethod paymentMethod) {
		if (paymentMethod == null) {
			return "";
		}
		return switch (paymentMethod) {
		case PIX -> "PIX";
		case CASH -> "Dinheiro";
		case CARD -> "Cartao";
		case INVOICE -> "Faturado";
		};
	}
}
