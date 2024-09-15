package com.hkust.isom5260.service;

import net.sf.jasperreports.engine.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Service
public class JasperReportService {
    public void generateReport(HttpServletResponse response, Properties properties) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"");
        try (OutputStream outputStream = response.getOutputStream()) {
            marshallJasperReport(outputStream,properties);
        } catch (JRException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report generation failed");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void marshallJasperReport(OutputStream outputStream , Properties properties) throws SQLException, JRException {
        Connection conn = DriverManager.getConnection(properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password"));
        String reportPath = "src/main/resources/isom5260.jrxml";
        JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, conn);
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    }




}
