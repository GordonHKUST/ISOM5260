package com.hkust.isom5260;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.hkust.isom5260.model.*;
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
	private PSSUSUserMapper pssusUserMapper;
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

	@GetMapping("activityDetail")
	public String activityDetail(Model model,@RequestParam(value="id",required = false) Integer id,
								 @RequestParam(value="bookingId",required = false) Integer bookingId,
								 Principal principal) {
		if(principal == null) {
			return "login";
		}
		boolean isBooking = false;
		boolean isJoined = false;
		USTStudent ustStudent = pssusUserMapper.selectByEmail(principal.getName());
		model.addAttribute("student", ustStudent);
		PSSUSBookingRecord bookingRecord = null;
		if (bookingId != null) {
			bookingRecord = pssusBookingMapper.getPSSUSBookingRecordByBookingId(String.valueOf(bookingId)).get(0);
            id = Integer.valueOf(bookingRecord.getSchedule_id());
			isBooking = true;
			if(pssusBookingMapper.getCountPSSUSBookingRecordByBookingId(String.valueOf(bookingId),ustStudent.getEmail()) > 0) {
				isJoined = true;
			}
		}
		List<LCSDSoccerPitchSchedule> schedules =
				lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(id);
		model.addAttribute("isBooking", isBooking);
		model.addAttribute("scheduleId", id);
		model.addAttribute("schedules", schedules.get(0));
		model.addAttribute("bookingRecord",bookingRecord);
		model.addAttribute("isJoined", isJoined);
		return "activityDetail";
	}

	@GetMapping("getRegisterActivity")
	@ResponseBody
	public List<PSSUSBookingRecord> getRegisterActivity(Model model, Principal principal) {
		String email = principal.getName();
		List<PSSUSBookingRecord> schedules = pssusBookingMapper.getMyPSSUSBookingRecordByEmail(email);
		for(PSSUSBookingRecord record : schedules) {
			LCSDSoccerPitchSchedule schedule = lcsdSoccerPitchScheduleMapper
					.getLCSDSoccerPitchScheduleById(Integer.valueOf(record.getSchedule_id())).get(0);
			USTStudent registStudent = pssusUserMapper.selectByEmail(record.getEmail());
			record.setFirst_Name(registStudent.getFirstName());
			record.setLast_Name(registStudent.getLastName());
			record.setEmail(registStudent.getEmail());
			record.setSession_start_time(schedule.getSession_start_time());
			record.setSession_end_time(schedule.getSession_end_time());
			record.setAvailable_date(schedule.getAvailable_date());
			record.setVenue_Name_En(schedule.getVenue_name_en());
		}
		return schedules;
	}


	@GetMapping("getOtherRegisterActivity")
	@ResponseBody
	public List<PSSUSBookingRecord> getOtherRegisterActivity(Model model, Principal principal) {
		String email = principal.getName();
		List<PSSUSBookingRecord> schedules = pssusBookingMapper.getOtherActivePSSUSBookingRecord(email);
		for(PSSUSBookingRecord record : schedules) {
			LCSDSoccerPitchSchedule schedule = lcsdSoccerPitchScheduleMapper
					.getLCSDSoccerPitchScheduleById(Integer.valueOf(record.getSchedule_id())).get(0);
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
		USTStudent ustStudent = pssusUserMapper.selectByEmail(principal.getName());
		model.addAttribute("student" , ustStudent.getEmail());
		return "searchPanel";
	}

	@PostMapping("/process_register")
	public ResponseEntity<?> processRegister(USTStudent USTStudent ,  BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest()
					.body("{\"success\": false, \"message\": " +
							result.getFieldError().getRejectedValue() + "}");
		}
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTStudent.getPassword());
		USTStudent.setPassword(encodedPassword);
		USTStudentWallet wallet = populateNewStudentBalance(USTStudent);
		pssusUserMapper.insert(USTStudent);
		pssusUserMapper.insert_wallet(wallet);
		return ResponseEntity.ok()
				.body("{\"success\": true, \"message\": \"User Registration successful\"}");
	}

	private static USTStudentWallet populateNewStudentBalance(USTStudent USTStudent) {
		USTStudentWallet wallet = new USTStudentWallet();
		wallet.setCurrBalance(3000);
		wallet.setLastMonthBalanceLeft(0);
		wallet.setEmail(USTStudent.getEmail());
		return wallet;
	}

	@PostMapping("/actJoin")
	public ResponseEntity<?>  processActivityJoin(@RequestBody PSSUSBookingRecord record,
												  Model model,
												  BindingResult result ,
												  HttpServletResponse response ,
												  Principal principal) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
           PSSUSJoinBookingRecord joinBookingRecord = new PSSUSJoinBookingRecord();
		   joinBookingRecord.setBooking_record_id(record.getBooking_Id());
		   USTStudent joiner = pssusUserMapper.selectByEmail(principal.getName());
		   joinBookingRecord.setJoiner_email(joiner.getEmail());
		   joinBookingRecord.setJoiner_student_id(joiner.getStudentId());
		   pssusBookingMapper.insertJoinRecord(joinBookingRecord);
		   return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Join successful\"}");
		}
	}

	@PostMapping("/actReg")
	public ResponseEntity<?>  processActivityRegister(@RequestBody PSSUSBookingRecord record, Model model, BindingResult result ,HttpServletResponse response) {
        pssusActivityRegisterValidator.validate(record,result);
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
			LCSDSoccerPitchSchedule lcsdSoccerPitchSchedule =
					lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(Integer.valueOf(record.getSchedule_id())).get(0);
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
			pssusBookingMapper.insertBookingRecord(record);
		}
		return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Registration successful\"}");
	}

	@GetMapping("/triggerReport")
	public ResponseEntity<String> triggerReport(HttpServletResponse response) throws IOException {
		jasperReportService.generateReport(response,properties);
		return ResponseEntity.ok("Report Generated successfully");
	}
	
	@GetMapping("/users")
	public String greeting(Model model, Principal principal,HttpServletResponse response) throws JRException, SQLException, IOException {
		if(principal == null) {
			return "/login";
		}
		USTStudentWallet wallet = pssusBookingMapper.getUSTStudentWalletByEmail(principal.getName()).get(0);
		   if(wallet != null) {
		       model.addAttribute("walletAmt",wallet.getCurrBalance());
		   } else {
		   	   model.addAttribute("walletAmt","NaN");
		   }
		model.addAttribute("user", principal.getName());
		model.addAttribute("avaliableSoccerSize",lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule().size());
		model.addAttribute("youreventsize",pssusBookingMapper.getMyPSSUSBookingRecordByEmail(principal.getName()).size());
		return "greeting";
	}

	@RequestMapping(value="/getRecentAvaSch")
	public @ResponseBody List<LCSDSoccerPitchSchedule> getRecentAvaSch(Model model) {
		return lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule();
	}

	@GetMapping({"","/login"})
	public String login(Model model, @RequestParam(required = false) String error) {
		model.addAttribute("user", new USTStudent());
        if(error != null &&
				error.equals(String.valueOf(HttpStatus.UNAUTHORIZED.value()))) {
			model.addAttribute("error",  HttpStatus.UNAUTHORIZED.value() + " : Bad Credentials");
		}
		return "index";
	}
}
