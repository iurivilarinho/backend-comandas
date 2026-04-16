package com.br.food.util.excel;

import org.apache.poi.ss.usermodel.*;
import java.awt.Color;

public class StatusCellStyleResolver implements CellStyleResolver {

    private CellStyle canceladoStyle;
    private CellStyle concluidaStyle;
    private CellStyle pendenteStyle;
    private CellStyle emAndamentoStyle;
    private CellStyle naStyle;

    @Override
    public void applyStyle(Workbook workbook, Cell cell, Object cellValue) {
        if (cellValue == null) return;

        String valor = cellValue.toString().trim();

        if (canceladoStyle == null) {
            canceladoStyle     = createRgbStyle(workbook, new Color(229,115,115)); // #E57373
            concluidaStyle     = createRgbStyle(workbook, new Color(129,199,132)); // #81C784
            pendenteStyle      = createRgbStyle(workbook, new Color(255,241,118)); // #FFF176
            emAndamentoStyle   = createRgbStyle(workbook, new Color(100,181,246)); // #64B5F6
            naStyle            = createRgbStyle(workbook, new Color(189,189,189)); // #BDBDBD
        }

        if (equalsIgnoreCaseAccentless(valor, "Cancelado")) {
            cell.setCellStyle(canceladoStyle);
        } else if (equalsIgnoreCaseAccentless(valor, "Concluída") || equalsIgnoreCaseAccentless(valor, "Concluida")
                || equalsIgnoreCaseAccentless(valor, "Concluído")) {
            cell.setCellStyle(concluidaStyle);
        } else if (equalsIgnoreCaseAccentless(valor, "Pendente")) {
            cell.setCellStyle(pendenteStyle);
        } else if (equalsIgnoreCaseAccentless(valor, "Em andamento")) {
            cell.setCellStyle(emAndamentoStyle);
        } else if (equalsIgnoreCaseAccentless(valor, "N/A")) {
            cell.setCellStyle(naStyle);
        }
    }

    private CellStyle createRgbStyle(Workbook workbook, Color rgb) {
        CellStyle style = workbook.createCellStyle();
        var xssfStyle = (org.apache.poi.xssf.usermodel.XSSFCellStyle) style;

        xssfStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(rgb, null));
        xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(isDark(rgb) ? IndexedColors.WHITE.getIndex() : IndexedColors.BLACK.getIndex());
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        return style;
    }

    private boolean isDark(Color c) {
        double lum = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
        return lum < 150;
    }

    private boolean equalsIgnoreCaseAccentless(String a, String b) {
        return normalize(a).equalsIgnoreCase(normalize(b));
    }

    private String normalize(String s) {
        if (s == null) return "";
        String norm = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}", "");
    }
}
