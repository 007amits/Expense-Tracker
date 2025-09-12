package com.expense.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;

@Service
public class PdfService {

    public <T> ByteArrayInputStream createPdf(List<T> data, List<String> headers, String title, List<Function<T, String>> mappers) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph(title).setBold().setFontSize(20));

        Table table = new Table(UnitValue.createPercentArray(headers.size()));
        table.setWidth(UnitValue.createPercentValue(100));

        headers.forEach(header -> table.addHeaderCell(new Paragraph(header).setBold()));

        for (T item : data) {
            for (Function<T, String> mapper : mappers) {
                table.addCell(new Paragraph(mapper.apply(item)));
            }
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
