package com.hkust.isom5260;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hkust.isom5260.dto.USTStudent;
import com.hkust.isom5260.mapper.CarMapper;
import com.hkust.isom5260.mapper.UserMapper;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletResponse;

@Controller
public class AppController {
    @Autowired
	private CarMapper carMapper;

    @Autowired
	private UserMapper userMapper;
    @Autowired
    private View error;

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new USTStudent());
		return "signup_form";
	}

	@GetMapping("activityDetail")
	public String activityDetail(Model model,Principal principal) {
		return "activityDetail";
	}

	@PostMapping("/process_register")
	public String processRegister(USTStudent USTStudent) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTStudent.getPassword());
		USTStudent.setPassword(encodedPassword);
		userMapper.insert(USTStudent);
		return "register_success";
	}

	@GetMapping("/triggerReport")
	public ResponseEntity<String> triggerReport(HttpServletResponse response) throws IOException {
		generateReport(response);
		return ResponseEntity.ok("Report Generated successfully");
	}
	
	@GetMapping("/users")
	public String listUsers(Model model, Principal principal,HttpServletResponse response) throws JRException, SQLException, IOException {
		List<USTStudent> listUSTStudents = userMapper.selectAll();
		model.addAttribute("user", principal.getName());
		model.addAttribute("listUsers", listUSTStudents);
		return "greeting";
	}

	// method for util generateReport(response);

	public void generateReport(HttpServletResponse response) throws IOException {
		response.setContentType("application/pdf");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"");
		try (OutputStream outputStream = response.getOutputStream()) {
			marshallJasperReport(outputStream);
		} catch (JRException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report generation failed");
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

	private static void marshallJasperReport(OutputStream outputStream) throws SQLException, JRException {
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//imz409.ust.hk:1521/imz409", "isom5260", "isom5260");
		String reportPath = "/Users/gordonli/Desktop/ISOM5260 Project/src/main/resources/isom5260.jrxml";
		JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, conn);
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
	}

	@GetMapping({"","/login"})
	public String login(Model model, @RequestParam(required = false) String error) {
        if(error != null &&
				error.equals(String.valueOf(HttpStatus.UNAUTHORIZED.value()))) {
			model.addAttribute("error",  HttpStatus.UNAUTHORIZED.value() + " : Bad Credentials");
		}
		return "index";
	}

}
