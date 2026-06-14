package com.okanetransfer.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.FeeGridRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.FeeGridExportService;
import com.okanetransfer.util.SecurityUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FeeGridExportServiceImpl
        implements FeeGridExportService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private FeeGridRepository  feeGridRepository;
    @Autowired
    private CorridorRepository corridorRepository;
    @Autowired
    private AuditService auditService;

    public FeeGridExportServiceImpl(
            FeeGridRepository feeGridRepository,
            CorridorRepository corridorRepository) {
        this.feeGridRepository  = feeGridRepository;
        this.corridorRepository = corridorRepository;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream exportPdf(Long corridorId) {
        Corridor corridor = findCorridorOrThrow(corridorId);
        List<FeeGrid> grids =
                feeGridRepository.findByCorridor_IdAndActiveOrderByMinAmountAsc(
                        corridorId, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter   writer   = new PdfWriter(baos);
            PdfDocument pdfDoc   = new PdfDocument(writer);
            Document    document = new Document(pdfDoc);


            addPdfHeader(document, corridor);


            addFeeGridTable(document, grids);

            addPdfFooter(document);

            document.close();

        } catch (IOException e) {
            throw new RuntimeException(
                    "PDF generation failed: " + e.getMessage(), e);
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "EXPORT_FEE_GRID_PDF",
                "Corridor",
                corridorId,
                LocalDateTime.now() + " - Export PDF de la grille tarifaire du corridor "
                        + corridor.getSourceCountry() + " → "
                        + corridor.getDestinationCountry()
        );

        return baos;
    }


    @Override
    @Transactional
    public ByteArrayOutputStream exportAllPdf() {
        List<Corridor> corridors =
                corridorRepository.findByActive(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter   writer   = new PdfWriter(baos);
            PdfDocument pdfDoc   = new PdfDocument(writer);
            Document    document = new Document(pdfDoc);

            // Titre global
            Paragraph title = new Paragraph(
                    "Grilles Tarifaires — Tous les Corridors")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Exporté le : "
                            + LocalDateTime.now().format(DATE_FMT))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subtitle);

            for (Corridor corridor : corridors) {
                List<FeeGrid> grids =
                        feeGridRepository.findByCorridor_IdAndActiveOrderByMinAmountAsc(
                                corridor.getId(), true);

                if (grids.isEmpty()) continue;

                addPdfHeader(document, corridor);
                addFeeGridTable(document, grids);

                document.add(new Paragraph(" "));
            }

            addPdfFooter(document);
            document.close();

        } catch (IOException e) {
            throw new RuntimeException(
                    "PDF generation failed: " + e.getMessage(), e);
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "EXPORT_ALL_FEE_GRIDS_PDF",
                "Corridor",
                null,
                LocalDateTime.now() + " - Export PDF de toutes les grilles tarifaires"
        );

        return baos;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream exportCsv(Long corridorId) {
        Corridor corridor = findCorridorOrThrow(corridorId);
        List<FeeGrid> grids =
                feeGridRepository.findByCorridor_IdAndActiveOrderByMinAmountAsc(
                        corridorId, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(
                baos, StandardCharsets.UTF_8)) {

            writer.write("\uFEFF");

            CSVPrinter printer = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.builder()
                            .setHeader(getCsvHeaders())
                            .setDelimiter(';')
                            .build());

            String corridorLabel = corridor.getSourceCountry()
                    + " → " + corridor.getDestinationCountry();

            for (FeeGrid grid : grids) {
                printer.printRecord(
                        corridorLabel,
                        grid.getMinAmount().toPlainString(),
                        formatMax(grid.getMaxAmount()),
                        grid.getFixedFee().toPlainString(),
                        grid.getPercentageFee().toPlainString() + "%",
                        computeFeeDisplay(grid),
                        grid.getAgencyShare().toPlainString() + "%",
                        grid.getCentralShare().toPlainString() + "%",
                        grid.isActive() ? "Actif" : "Inactif"
                );
            }

            printer.flush();

        } catch (IOException e) {
            throw new RuntimeException(
                    "CSV generation failed: " + e.getMessage(), e);
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "EXPORT_FEE_GRID_CSV",
                "Corridor",
                corridorId,
                LocalDateTime.now() + " - Export CSV de la grille tarifaire du corridor "
                        + corridor.getSourceCountry() + " → "
                        + corridor.getDestinationCountry()
        );

        return baos;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream exportAllCsv() {
        List<Corridor> corridors =
                corridorRepository.findByActive(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(
                baos, StandardCharsets.UTF_8)) {

            writer.write("\uFEFF");

            CSVPrinter printer = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.builder()
                            .setHeader(getCsvHeaders())
                            .setDelimiter(';')
                            .build());

            for (Corridor corridor : corridors) {
                List<FeeGrid> grids =
                        feeGridRepository.findByCorridor_IdAndActiveOrderByMinAmountAsc(
                                corridor.getId(), true);

                if (grids.isEmpty()) continue;

                String corridorLabel =
                        corridor.getSourceCountry()
                                + " → "
                                + corridor.getDestinationCountry();

                for (FeeGrid grid : grids) {
                    printer.printRecord(
                            corridorLabel,
                            grid.getMinAmount().toPlainString(),
                            formatMax(grid.getMaxAmount()),
                            grid.getFixedFee().toPlainString(),
                            grid.getPercentageFee().toPlainString()
                                    + "%",
                            computeFeeDisplay(grid),
                            grid.getAgencyShare().toPlainString()
                                    + "%",
                            grid.getCentralShare().toPlainString()
                                    + "%",
                            grid.isActive() ? "Actif" : "Inactif"
                    );
                }
            }

            printer.flush();

        } catch (IOException e) {
            throw new RuntimeException(
                    "CSV generation failed: " + e.getMessage(), e);
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "EXPORT_ALL_FEE_GRIDS_CSV",
                "Corridor",
                null,
                LocalDateTime.now() + " - Export CSV de toutes les grilles tarifaires"
        );

        return baos;
    }

    private void addPdfHeader(Document document,
                              Corridor corridor)
            throws IOException {

        String corridorLabel = corridor.getSourceCountry()
                + " → " + corridor.getDestinationCountry();

        String devises = corridor.getSourceCurrency().getCode()
                + " → "
                + corridor.getDestinationCurrency().getCode();

        Paragraph corridorTitle = new Paragraph(
                "Grille Tarifaire — " + corridorLabel
                        + "  (" + devises + ")")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(3);
        document.add(corridorTitle);

        Paragraph exportDate = new Paragraph(
                "Exporté le : "
                        + LocalDateTime.now().format(DATE_FMT))
                .setFontSize(8)
                .setMarginBottom(10);
        document.add(exportDate);
    }

    private void addFeeGridTable(Document document,
                                 List<FeeGrid> grids)
            throws IOException {

        Table table = new Table(UnitValue.createPercentArray(
                new float[]{10, 10, 10, 10, 12, 14, 12, 12}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {
                "Montant min", "Montant max",
                "Frais fixes", "Frais %",
                "Frais total affiché",
                "Part agence", "Part centrale", "Statut"
        };

        for (String h : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(h)
                            .setBold()
                            .setFontSize(8))
                    .setBackgroundColor(
                            new com.itextpdf.kernel.colors
                                    .DeviceRgb(0, 128, 128))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            table.addHeaderCell(headerCell);
        }

        boolean alternate = false;
        for (FeeGrid grid : grids) {

            com.itextpdf.kernel.colors.Color rowColor =
                    alternate
                            ? new com.itextpdf.kernel.colors
                            .DeviceRgb(240, 248, 248)
                            : ColorConstants.WHITE;
            alternate = !alternate;

            String[] values = {
                    formatAmount(grid.getMinAmount()),
                    formatMax(grid.getMaxAmount()),
                    formatAmount(grid.getFixedFee()) + " MAD",
                    grid.getPercentageFee().toPlainString() + "%",
                    computeFeeDisplay(grid),
                    grid.getAgencyShare().toPlainString() + "%",
                    grid.getCentralShare().toPlainString() + "%",
                    grid.isActive() ? "Actif" : "Inactif"
            };

            for (String val : values) {
                Cell cell = new Cell()
                        .add(new Paragraph(val).setFontSize(9))
                        .setBackgroundColor(rowColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(4);
                table.addCell(cell);
            }
        }

        if (!grids.isEmpty()) {
            BigDecimal avgAgency = grids.stream()
                    .map(FeeGrid::getAgencyShare)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(grids.size()), 1,
                            java.math.RoundingMode.HALF_UP);

            BigDecimal avgCentral = BigDecimal.valueOf(100)
                    .subtract(avgAgency);

            Cell summaryCell = new Cell(1, 8)
                    .add(new Paragraph(
                            "Répartition moyenne sur ce corridor"
                                    + "  —  Agence : " + avgAgency + "%"
                                    + "   Centrale : " + avgCentral + "%")
                            .setFontSize(8)
                            .setBold())
                    .setBackgroundColor(
                            new com.itextpdf.kernel.colors
                                    .DeviceRgb(230, 245, 245))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addCell(summaryCell);
        }

        document.add(table);
    }

    private void addPdfFooter(Document document) {
        Paragraph footer = new Paragraph(
                "Document confidentiel — OkaneTransfer"
                        + " — Généré automatiquement")
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);
    }

    private String[] getCsvHeaders() {
        return new String[]{
                "Corridor",
                "Montant min (MAD)",
                "Montant max (MAD)",
                "Frais fixes (MAD)",
                "Frais en %",
                "Frais total affiché",
                "Part agence",
                "Part centrale",
                "Statut"
        };
    }

    private String computeFeeDisplay(FeeGrid grid) {
        if (grid.getPercentageFee().compareTo(BigDecimal.ZERO) == 0
                && grid.getFixedFee().compareTo(
                BigDecimal.ZERO) > 0) {
            return formatAmount(grid.getFixedFee()) + " MAD";
        }
        return "Calculé";
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    private String formatMax(BigDecimal maxAmount) {
        if (maxAmount == null) return "Illimite";
        if (maxAmount.compareTo(
                BigDecimal.valueOf(9_999_999)) >= 0) {
            return "Illimite";
        }
        return formatAmount(maxAmount);
    }

    private Corridor findCorridorOrThrow(Long corridorId) {
        return corridorRepository.findById(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found: " + corridorId));
    }
}
