package com.okanetransfer.service;

import java.io.ByteArrayOutputStream;

public interface FeeGridExportService {

    ByteArrayOutputStream exportPdf(Long corridorId);

    ByteArrayOutputStream exportCsv(Long corridorId);

    ByteArrayOutputStream exportAllPdf();

    ByteArrayOutputStream exportAllCsv();
}