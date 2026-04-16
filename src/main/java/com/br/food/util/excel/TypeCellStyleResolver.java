package com.br.food.util.excel;

import org.apache.poi.ss.usermodel.*;

public class TypeCellStyleResolver implements CellStyleResolver {

	private CellStyle projeto;
	private CellStyle entrega;
	private CellStyle acao;
	private CellStyle tarefa;

	@Override
	public void applyStyle(Workbook workbook, Cell cell, Object cellValue) {

		if (cellValue == null)
			return;

		String valor = cellValue.toString().trim().toUpperCase();

		if (projeto == null) {
			projeto = create(workbook, IndexedColors.GREY_80_PERCENT);
			entrega = create(workbook, IndexedColors.GREY_50_PERCENT);
			acao = create(workbook, IndexedColors.GREY_40_PERCENT);
			tarefa = create(workbook, IndexedColors.GREY_25_PERCENT);
		}

		switch (valor) {
		case "PROJETO":
			cell.setCellStyle(projeto);
			break;

		case "ENTREGA":
			cell.setCellStyle(entrega);
			break;

		case "AÇÃO":
		case "ACAO":
			cell.setCellStyle(acao);
			break;

		case "TAREFA":
			cell.setCellStyle(tarefa);
			break;
		}
	}

	private CellStyle create(Workbook wb, IndexedColors color) {
		CellStyle style = wb.createCellStyle();
		style.setFillForegroundColor(color.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(true);

		// Cria fonte
		Font font = wb.createFont();

		// Cores escuras: deixa fonte branca
		if (isDarkColor(color)) {
			font.setColor(IndexedColors.WHITE.getIndex());
		} else {
			font.setColor(IndexedColors.BLACK.getIndex());
		}

		// Define negrito sempre
		font.setBold(true);

		style.setFont(font);

		return style;
	}

	private boolean isDarkColor(IndexedColors color) {
		// Ajuste conforme desejar — aqui assumo que tons acima de 50% são considerados
		// escuros
		switch (color) {
		case GREY_80_PERCENT:
		case GREY_50_PERCENT:
		case GREY_40_PERCENT:
			return true;
		default:
			return false;
		}
	}

}
