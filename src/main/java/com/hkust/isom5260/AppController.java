package com.hkust.isom5260;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.hkust.isom5260.dto.BookingRecord;
import com.hkust.isom5260.dto.LCSDDistrict;
import com.hkust.isom5260.dto.LCSDSoccerPitchSchedule;
import com.hkust.isom5260.dto.USTStudent;
import com.hkust.isom5260.mapper.CarMapper;
import com.hkust.isom5260.mapper.DistrictMapper;
import com.hkust.isom5260.mapper.LCSDSoccerPitchScheduleMapper;
import com.hkust.isom5260.mapper.UserMapper;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletResponse;

@Controller
public class AppController {
	private static Properties properties = new Properties();
    @Autowired
	private CarMapper carMapper;
    @Autowired
	private UserMapper userMapper;
	@Autowired
	private LCSDSoccerPitchScheduleMapper lcsdSoccerPitchScheduleMapper;
	@Autowired
	private DistrictMapper districtMapper;

    @Autowired
    private View error;

	public AppController() {
		String propertiesFilePath = "src/main/resources/application.properties";
		try (InputStream input = Files.newInputStream(Paths.get(propertiesFilePath))) {
			properties.load(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new USTStudent());
		return "signup_form";
	}

	@GetMapping("activityDetail")
	public String activityDetail(Model model,@RequestParam int id, Principal principal) {
		List<LCSDSoccerPitchSchedule> schedules = lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(id);
		USTStudent ustStudent = userMapper.selectByEmail(principal.getName());
		model.addAttribute("student",ustStudent);
		model.addAttribute("schedules",schedules.get(0));
		return "activityDetail";
	}

	@GetMapping("searchPanel")
	public String searchPanel(Model model,Principal principal) {
		//USTStudent ustStudent = userMapper.selectByEmail(principal.getName());
//		List<LCSDDistrict> lcsdDistricts = districtMapper.selectDistrictFromSchedule();
//		model.addAttribute("districts", lcsdDistricts);
		//model.addAttribute("student" , ustStudent);
		return "searchPanel";
	}

	@PostMapping("/process_register")
	public String processRegister(USTStudent USTStudent) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTStudent.getPassword());
		USTStudent.setPassword(encodedPassword);
		userMapper.insert(USTStudent);
		return "register_success";
	}

	@PostMapping("/actReg")
	public String processActivityRegister(@RequestBody BookingRecord record, Model model, HttpServletResponse response) {
		if(record == null) {
			return "";
		}
		return "greeting";
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

	@RequestMapping(value="/getRecentAvaSch")
	public @ResponseBody List<LCSDSoccerPitchSchedule> getRecentAvaSch(Model model) {
		return lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule();
	}

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
        Connection conn = DriverManager.getConnection(properties.getProperty("spring.datasource.url"),
				properties.getProperty("spring.datasource.username"),
				properties.getProperty("spring.datasource.password"));
		String reportPath = "src/main/resources/isom5260.jrxml";
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
