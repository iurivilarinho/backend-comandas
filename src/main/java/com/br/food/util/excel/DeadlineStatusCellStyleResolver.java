package com.br.food.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.Color;

public class DeadlineStatusCellStyleResolver implements CellStyleResolver {

	private CellStyle emDiaStyle;
	private CellStyle atrasadoStyle;
	private CellStyle naStyle;

	@Override
	public void applyStyle(Workbook workbook, Cell cell, Object cellValue) {
		if (cellValue == null) {
			return;
		}

		String valor = cellValue.toString().trim().toUpperCase();

		if (emDiaStyle == null) {
			// Em dia -> verde suave
			emDiaStyle = createRgbStyle(workbook, new Color(129, 199, 132)); // #81C784
			// Atrasado -> vermelho forte
			atrasadoStyle = createRgbStyle(workbook, new Color(198, 40, 40)); // #C62828
			// N/A -> cinza neutro
			naStyle = createRgbStyle(workbook, new Color(189, 189, 189)); // #BDBDBD
		}

		switch (valor) {
		case "EM DIA":
			cell.setCellStyle(emDiaStyle);
			break;

		case "ATRASADO":
			cell.setCellStyle(atrasadoStyle);
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
