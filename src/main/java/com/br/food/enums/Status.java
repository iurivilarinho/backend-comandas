package com.br.food.enums;

public class Status {

	public static enum StatusPedido {
		ANDAMENTO, A_CAMINHO, CANCELADO, AGUARDANDO_APROVACAO, FINALIZADO
	}

	public static enum StatusItem {
		ATENDIDO, EM_PREPARO, PENDENTE, RECUSADO
	}

	public static enum StatusNotaFiscal {
		ALOCADA, EM_CONSUMO, CONSUMIDA, CANCELADA
	}

}
