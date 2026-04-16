package com.br.food.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.Color;

public class PriorityCellStyleResolver implements CellStyleResolver {

	private CellStyle criticalStyle;
	private CellStyle highStyle;
	private CellStyle mediumStyle;
	private CellStyle lowStyle;
	private CellStyle naStyle;

	@Override
	public void applyStyle(Workbook workbook, Cell cell, Object cellValue) {
		if (cellValue == null)
			return;

		String valor = cellValue.toString().trim().toUpperCase();

		if (criticalStyle == null) {
			criticalStyle = createRgbStyle(workbook, new Color(198, 40, 40)); // Vermelho CRITICAL
			highStyle = createRgbStyle(workbook, new Color(255, 143, 0)); // Laranja HIGH
			mediumStyle = createRgbStyle(workbook, new Color(255, 238, 88)); // Amarelo MEDIUM
			lowStyle = createRgbStyle(workbook, new Color(129, 199, 132)); // Verde LOW
			naStyle = createRgbStyle(workbook, new Color(189, 189, 189)); // Cinza N/A
		}

		switch (valor) {
		case "CRÍTICA":
			cell.setCellStyle(criticalStyle);
			break;

		case "ALTA":
			cell.setCellStyle(highStyle);
			break;

		case "MÉDIA":
			cell.setCellStyle(mediumStyle);
			break;

		case "BAIXA":
			cell.setCellStyle(lowStyle);
			break;

		case "N/A":
		case "NA":
			cell.setCellStyle(naStyle);
			break;

		default:
			// sem cor
		}
	}

	private CellStyle createRgbStyle(Workbook workbook, Color color) {
		CellStyle style = workbook.createCellStyle();
		XSSFCellStyle xssfStyle = (XSSFCellStyle) style;

		xssfStyle.setFillForegroundColor(new XSSFColor(color, null));
		xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		Font font = workbook.createFont();
		font.setBold(true);
		font.setColor(isDark(color) ? IndexedColors.WHITE.getIndex() : IndexedColors.BLACK.getIndex());
		style.setFont(font);

		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(true);

		return style;
	}

	private boolean isDark(Color c) {
		double luminancia = (0.299 * c.getRed()) + (0.587 * c.getGreen()) + (0.114 * c.getBlue());

		return luminancia < 150;
	}
}
