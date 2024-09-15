package com.hkust.isom5260;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.hkust.isom5260.dto.PSSUSBookingRecord;
import com.hkust.isom5260.dto.LCSDDistrict;
import com.hkust.isom5260.dto.LCSDSoccerPitchSchedule;
import com.hkust.isom5260.dto.USTStudent;
import com.hkust.isom5260.mapper.LCSDDistrictMapper;
import com.hkust.isom5260.mapper.LCSDSoccerPitchScheduleMapper;
import com.hkust.isom5260.mapper.PSSUSBookingMapper;
import com.hkust.isom5260.mapper.PSSUSUserMapper;
import com.hkust.isom5260.service.JasperReportService;
import com.hkust.isom5260.validators.PSSUSActivityRegisterValidator;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletResponse;

@Controller
public class AppController {
	private static Properties properties = new Properties();
    @Autowired
	private PSSUSUserMapper PSSUSUserMapper;
	@Autowired
	private LCSDSoccerPitchScheduleMapper lcsdSoccerPitchScheduleMapper;
	@Autowired
	private LCSDDistrictMapper districtMapper;
    @Autowired
	private JasperReportService jasperReportService;
    @Autowired
	private PSSUSActivityRegisterValidator pssusActivityRegisterValidator;
	@Autowired
	private PSSUSBookingMapper pssusBookingMapper;
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
	public String activityDetail(Model model,@RequestParam(value="id",required = false) Integer id,
								 @RequestParam(value="bookingId",required = false) Integer bookingId,
								 Principal principal) {
		USTStudent ustStudent = PSSUSUserMapper.selectByEmail(principal.getName());
		model.addAttribute("student", ustStudent);
		if (bookingId != null) {
            PSSUSBookingRecord bookingRecord =
					pssusBookingMapper.getPSSUSBookingRecordByBookingId(String.valueOf(bookingId)).get(0);
            id = Integer.valueOf(bookingRecord.getSchedule_Id());
		}
		List<LCSDSoccerPitchSchedule> schedules =
				lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(id);
		model.addAttribute("scheduleId", id);
		model.addAttribute("schedules", schedules.get(0));
		return "activityDetail";
	}

	@GetMapping("getRegisterActivity")
	@ResponseBody
	public List<PSSUSBookingRecord> getRegisterActivity(Model model, Principal principal) {
		String email = principal.getName();
		List<PSSUSBookingRecord> schedules = pssusBookingMapper.getPSSUSBookingRecordByEmail(email);
		for(PSSUSBookingRecord record : schedules) {
			LCSDSoccerPitchSchedule schedule = lcsdSoccerPitchScheduleMapper
					.getLCSDSoccerPitchScheduleById(Integer.valueOf(record.getSchedule_Id())).get(0);
			record.setSession_start_time(schedule.getSession_start_time());
			record.setSession_end_time(schedule.getSession_end_time());
			record.setAvailable_date(schedule.getAvailable_date());
			record.setVenue_Name_En(schedule.getVenue_name_en());
		}
		return schedules;
	}

	@GetMapping("searchPanel")
	public String searchPanel(Model model,Principal principal) {
		List<LCSDDistrict> lcsdDistricts = districtMapper.selectDistrictFromSchedule();
		model.addAttribute("districts", lcsdDistricts);
		// do it in last step
		//USTStudent ustStudent = userMapper.selectByEmail(principal.getName());
		//model.addAttribute("student" , ustStudent);
		return "searchPanel";
	}

	@PostMapping("/process_register")
	public String processRegister(USTStudent USTStudent) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTStudent.getPassword());
		USTStudent.setPassword(encodedPassword);
		PSSUSUserMapper.insert(USTStudent);
		return "register_success";
	}

	@PostMapping("/actReg")
	public ResponseEntity<?>  processActivityRegister(@RequestBody PSSUSBookingRecord record, Model model, BindingResult result ,HttpServletResponse response) {
        pssusActivityRegisterValidator.validate(record,result);
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
			LCSDSoccerPitchSchedule lcsdSoccerPitchSchedule =
					lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(Integer.valueOf(record.getSchedule_Id())).get(0);
			int avaliableCount = Integer.valueOf(lcsdSoccerPitchSchedule.getAvailable_courts()) - 1;
			if(avaliableCount <= 0) {
				lcsdSoccerPitchSchedule.setAvailable_courts(String.valueOf(0));
				lcsdSoccerPitchSchedule.setStatus_code("FULL_BOOKING");
				lcsdSoccerPitchScheduleMapper.update(lcsdSoccerPitchSchedule);
			} else {
				lcsdSoccerPitchSchedule.setAvailable_courts(String.valueOf(avaliableCount));
				lcsdSoccerPitchScheduleMapper.update(lcsdSoccerPitchSchedule);
			}
			record.setStatus_code("PENDING_APPROVAL");
			pssusBookingMapper.insert(record);
		}
		return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Registration successful\"}");
	}

	@GetMapping("/triggerReport")
	public ResponseEntity<String> triggerReport(HttpServletResponse response) throws IOException {
		jasperReportService.generateReport(response,properties);
		return ResponseEntity.ok("Report Generated successfully");
	}
	
	@GetMapping("/users")
	public String listUsers(Model model, Principal principal,HttpServletResponse response) throws JRException, SQLException, IOException {
		List<USTStudent> listUSTStudents = PSSUSUserMapper.selectAll();
		model.addAttribute("user", principal.getName());
		model.addAttribute("listUsers", listUSTStudents);
		return "greeting";
	}

	@RequestMapping(value="/getRecentAvaSch")
	public @ResponseBody List<LCSDSoccerPitchSchedule> getRecentAvaSch(Model model) {
		return lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule();
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
