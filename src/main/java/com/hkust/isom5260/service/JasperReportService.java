package com.hkust.isom5260.service;

import com.hkust.isom5260.model.AdminReportCriteria;
import net.sf.jasperreports.engine.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class JasperReportService {

    public void generateAdminReport(AdminReportCriteria criteria, HttpServletResponse response, Properties properties, Principal principal) throws IOException, SQLException {
        Connection conn = DriverManager.getConnection(properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password"));
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"");
        try (OutputStream outputStream = response.getOutputStream()) {
            Map<String, Object> parameters = new HashMap<>();
            String reportPath = "";
            switch (criteria.getReportSelect()) {
                case "report1":
                    reportPath = "src/main/resources/report1.jrxml";
                    break;
                case "report2":
                    reportPath = "src/main/resources/report2.jrxml";
                    break;
                case "report3":
                    reportPath = "src/main/resources/report3.jrxml";
                    break;
                default:
                    reportPath = "default/path/to/report.jrxml";
                    break;
            }
            parameters.put("startDate", Date.valueOf(criteria.getStartDate()));
            parameters.put("endDate", Date.valueOf(criteria.getEndDate()));
            parameters.put("program",criteria.getProgram());
            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        } catch (JRException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report generation failed");
        }
    }

    public void generateReport(HttpServletResponse response, Properties properties, Principal principal) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"");
        try (OutputStream outputStream = response.getOutputStream()) {
            monthlyStmtGenerate(outputStream,properties, principal.getName());
        } catch (JRException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report generation failed");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void monthlyStmtGenerate(OutputStream outputStream , Properties properties, String email) throws SQLException, JRException {
        Connection conn = DriverManager.getConnection(properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password"));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", email);
        String reportPath = "src/main/resources/isom5260.jrxml";
        JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    }
}
