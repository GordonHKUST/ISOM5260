package com.hkust.isom5260;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.hkust.isom5260.model.*;
import com.hkust.isom5260.mapper.LCSDDistrictMapper;
import com.hkust.isom5260.mapper.LCSDSoccerPitchScheduleMapper;
import com.hkust.isom5260.mapper.PSSUSBookingMapper;
import com.hkust.isom5260.mapper.PSSUSUserMapper;
import com.hkust.isom5260.service.JasperReportService;
import com.hkust.isom5260.validators.PSSUSUserValidator;
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
import org.thymeleaf.util.StringUtils;

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
	private PSSUSBookingMapper pssusBookingMapper;
	@Autowired
	private PSSUSUserValidator pssusUserValidator;
    @Autowired
    private View error;

	private Connection conn;

    public AppController() throws SQLException {
		String propertiesFilePath = "src/main/resources/application.properties";
		try (InputStream input = Files.newInputStream(Paths.get(propertiesFilePath))) {
			properties.load(input);
			conn = DriverManager.getConnection(properties.getProperty("spring.datasource.url"),
					properties.getProperty("spring.datasource.username"),
					properties.getProperty("spring.datasource.password"));
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
		USTUser USTUser = pssusUserMapper.selectByEmail(principal.getName());
		model.addAttribute("student", USTUser);
		PSSUSBookingRecord bookingRecord = null;
		if (bookingId != null) {
			bookingRecord = pssusBookingMapper.getPSSUSBookingRecordByBookingId(String.valueOf(bookingId)).get(0);
			USTUser host = pssusUserMapper.selectByEmail(bookingRecord.getEmail());
			bookingRecord.setLast_Name(host.getLastName());
			bookingRecord.setFirst_Name(host.getFirstName());
					id = Integer.valueOf(bookingRecord.getSchedule_id());
			isBooking = true;
			if(pssusBookingMapper.getCountPSSUSBookingRecordByBookingId(String.valueOf(bookingId),USTUser.getEmail()) > 0) {
				isJoined = true;
			}
		}
		List<LCSDSoccerPitchSchedule> schedules =
				lcsdSoccerPitchScheduleMapper.getLCSDSoccerPitchScheduleById(id);
	    if(StringUtils.equals(USTUser.getRight(),"STUDENT")) {
	  	    model.addAttribute("right", "STUDENT");
	    }
        else if(StringUtils.equals(USTUser.getRight(),"ADMIN")) {
	  		model.addAttribute("right", "ADMIN");
	    }
		model.addAttribute("user",principal.getName());
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
			USTUser registStudent = pssusUserMapper.selectByEmail(record.getEmail());
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


	@GetMapping("getPendingPSSUSBookingRecord")
	@ResponseBody
	public List<PSSUSBookingRecord> getPendingPSSUSBookingRecord(Model model, Principal principal) {
		String email = principal.getName();
		List<PSSUSBookingRecord> schedules = pssusBookingMapper.getPendingPSSUSBookingRecord();
		return getPssusBookingRecords(schedules);
	}

	private List<PSSUSBookingRecord> getPssusBookingRecords(List<PSSUSBookingRecord> schedules) {
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

	@GetMapping("getOtherRegisterActivity")
	@ResponseBody
	public List<PSSUSBookingRecord> getOtherRegisterActivity(Model model, Principal principal) {
		String email = principal.getName();
		List<PSSUSBookingRecord> schedules = pssusBookingMapper.getOtherActivePSSUSBookingRecord(email);
		return getPssusBookingRecords(schedules);
	}

	@GetMapping("getJoinerOfEvent")
	@ResponseBody
	public List<USTUser> getJoinerOfEvent(@RequestParam String bookingId,  Model model) {
        return pssusBookingMapper.getJoinUserByBookingId(String.valueOf(Integer.parseInt(bookingId)));
	}

	@GetMapping("getTxnRecordByUser")
	@ResponseBody
	public List<USTStudentWalletTransaction> getTxnRecordByUser(Model model, Principal principal) {
		String email = principal.getName();
		USTStudentWallet ustStudentWallet = pssusBookingMapper.getUSTStudentWalletByEmail(email).get(0);
		List<USTStudentWalletTransaction> txnList = pssusBookingMapper.getUSTStudentWalletTxnByWalletId(ustStudentWallet.getWallet_id());
		return txnList;
	}

	@GetMapping("searchPanel")
	public String searchPanel(Model model,Principal principal) {
		if(principal == null) {
			return "login";
		}
		List<LCSDDistrict> lcsdDistricts = districtMapper.selectDistrictFromSchedule();
		model.addAttribute("districts", lcsdDistricts);
		USTUser USTUser = pssusUserMapper.selectByEmail(principal.getName());
		if(StringUtils.equals(USTUser.getRight(),"STUDENT")) {
			model.addAttribute("right", "STUDENT");
		}
		else if(StringUtils.equals(USTUser.getRight(),"ADMIN")) {
			model.addAttribute("right", "ADMIN");
		}
		model.addAttribute("user" , USTUser.getEmail());
		return "searchPanel";
	}

	@PostMapping("/process_register")
	public ResponseEntity<?> processRegister(USTUser USTUser ,  BindingResult result) {
		pssusUserValidator.validate(USTUser,result);
		if (result.hasErrors()) {
			List<String> errorMessages = result.getFieldErrors().stream()
					.map(error -> String.format("%s", error.getDefaultMessage()))
					.collect(Collectors.toList());
			String errorResponse = String.format("{\"success\": false, \"errors\": [%s]}",
					errorMessages.stream().map(msg -> "\"" + msg + "\"").collect(Collectors.joining(", ")));
			return ResponseEntity.badRequest().body(errorResponse);
		}
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTUser.getPassword());
		USTUser.setPassword(encodedPassword);
		USTUser.setRight("STUDENT");
		USTStudentWallet wallet = populateNewStudentBalance(USTUser);
		pssusUserMapper.insert(USTUser);
		pssusUserMapper.insert_wallet(wallet);
		USTStudentWalletTransaction walletTransaction = new USTStudentWalletTransaction();
		USTStudentWallet newCreatedWallet = pssusBookingMapper.getUSTStudentWalletByEmail(USTUser.getEmail()).get(0);
		Date transactionDate = new Date(); // Example transaction date
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		populateWalletTxn(formatter, transactionDate, walletTransaction, newCreatedWallet);
		return ResponseEntity.ok()
				.body("{\"success\": true, \"message\": \"User Registration successful\"}");
	}

	private void populateWalletTxn(SimpleDateFormat formatter, Date transactionDate, USTStudentWalletTransaction walletTransaction, USTStudentWallet wallet) {
		String dateString = formatter.format(transactionDate);
		walletTransaction.setTxn_date(dateString);
		walletTransaction.setTransactionLog("NEW MEMBER CREATION");
		walletTransaction.setAmount(3000);
		walletTransaction.setAction_name("CR");
		walletTransaction.setWalletId(wallet.getWallet_id());
		pssusBookingMapper.insertStudentWalletTransaction(walletTransaction);
	}

	private static USTStudentWallet populateNewStudentBalance(USTUser USTUser) {
		USTStudentWallet wallet = new USTStudentWallet();
		wallet.setCurrBalance(3000);
		wallet.setLastMonthBalanceLeft(0);
		wallet.setEmail(USTUser.getEmail());
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
		   USTUser joiner = pssusUserMapper.selectByEmail(principal.getName());
		   joinBookingRecord.setJoiner_email(joiner.getEmail());
		   joinBookingRecord.setJoiner_student_id(joiner.getStudentId());
		   Date transactionDate = new Date(); // Example transaction date
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = formatter.format(transactionDate);
		   joinBookingRecord.setRecord_create_date(dateString);
		   pssusBookingMapper.insertJoinRecord(joinBookingRecord);
		   return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Join successful\"}");
		}
	}

	@PostMapping("/approve")
	public ResponseEntity<?>  processActivityApprove(@RequestBody PSSUSBookingRecord record, Model model, BindingResult result ,HttpServletResponse response) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
			LCSDSoccerPitch pitch = null;
			record.setStatus_code("RECORD_APPROVED");
			USTUser user = pssusUserMapper.selectByEmail(record.getEmail());
			USTStudentWallet wallet  = (USTStudentWallet) pssusBookingMapper.getUSTStudentWalletByEmail(record.getEmail()).get(0);
			LCSDSoccerPitchSchedule schedule = lcsdSoccerPitchScheduleMapper
					.getLCSDSoccerPitchScheduleById(Integer.parseInt(record.getSchedule_id()))
					.get(0);
			if(schedule != null) {
				pitch = pssusBookingMapper.getLCSDSoccerPitchByPitchType(schedule.getFacility_type_name_en()).get(0);
			} else {
				return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Current Schedule not found \"}");
			}
			USTStudentWalletTransaction transaction = new USTStudentWalletTransaction();
			if(pitch != null) {
				double balanceAfter = wallet.getCurrBalance() - Double.parseDouble(pitch.getPrice());
				wallet.setCurrBalance(balanceAfter);
				if (balanceAfter < 0) {
					return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Current Balance of student not fulfilled \"}");
				} else {
					pssusBookingMapper.updateWallet(wallet);
				}
				transaction.setAmount(Double.parseDouble(pitch.getPrice()));
				transaction.setAction_name("DR");
				transaction.setTransactionLog("VENUE_BOOKING_APPROVED");
				transaction.setWalletId(wallet.getWallet_id());
				Date transactionDate = new Date(); // Example transaction date
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateString = formatter.format(transactionDate);
				transaction.setTxn_date(dateString);
				transaction.setBooking_id(Integer.parseInt(record.getBooking_Id()));
				pssusBookingMapper.insertStudentWalletTransaction(transaction);
				pssusBookingMapper.adminBookingRecordAction(record);
			} else {
				return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Pitch cannot find, please check \"}");
			}
		}
		return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Approved successful\"}");
	}

	@PostMapping("/reject")
	public ResponseEntity<?>  processActivityReject(@RequestBody PSSUSBookingRecord record, Model model, BindingResult result ,HttpServletResponse response) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
			record.setStatus_code("RECORD_REJECTED");
			pssusBookingMapper.adminBookingRecordAction(record);
		}
		return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Rejected successful\"}");
	}

	@PostMapping("/actReg")
	public ResponseEntity<?>  processActivityRegister(@RequestBody PSSUSBookingRecord record, Model model, BindingResult result ,HttpServletResponse response) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body("{\"success\": false, \"message\": " + result.getFieldError().getRejectedValue() + "}");
		} else {
			record.setStatus_code("PENDING_APPROVAL");
			Date transactionDate = new Date(); // Example transaction date
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = formatter.format(transactionDate);
			record.setRecord_create_date(dateString);
			pssusBookingMapper.insertBookingRecord(record);
		}
		return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Campaign Registration successful\"}");
	}

	@GetMapping("/triggerReport")
	public ResponseEntity<String> triggerReport(Principal principal,HttpServletResponse response) throws IOException {
		jasperReportService.generateReport(response,properties, principal);
		return ResponseEntity.ok("Report Generated successfully");
	}

	@GetMapping("/adminReport")
	public String adminReport(Principal principal,Model model,HttpServletResponse response) throws IOException {
		if(principal == null) {
			return "login";
		}
		USTUser ustUser = pssusUserMapper.selectByEmail(principal.getName());
		if(StringUtils.equals(ustUser.getRight(),"STUDENT")) {
			model.addAttribute("right", "STUDENT");
		}
		else if(StringUtils.equals(ustUser.getRight(),"ADMIN")) {
			model.addAttribute("right", "ADMIN");
		}
		model.addAttribute("email",principal.getName());
		return "reportPrint";
	}

	@GetMapping("/triggerAdminReport")
	public ResponseEntity<String> triggerAdminReport(@RequestParam(name="reportSelect") String reportSelect,
													 @RequestParam(name="startDate") String startDate,
													 @RequestParam(name="endDate") String endDate,
													 @RequestParam(name="program") String program,
													 Principal principal,HttpServletResponse response) throws IOException, SQLException {
		AdminReportCriteria criteria = new AdminReportCriteria();
		criteria.setReportSelect(reportSelect);
		criteria.setStartDate(startDate);
		criteria.setEndDate(endDate);
		criteria.setProgram(program);
		jasperReportService.generateAdminReport(criteria,response,properties, principal);
		return ResponseEntity.ok("Report Generated successfully");
	}

	@GetMapping("/users")
	public String greeting(Model model, Principal principal,HttpServletResponse response) throws JRException, SQLException, IOException {
		if(principal == null) {
			return "/login";
		}
		USTUser ustUser = pssusUserMapper.selectByEmail(principal.getName());
		if(StringUtils.equals(ustUser.getRight(),"STUDENT")) {
			USTStudentWallet wallet = pssusBookingMapper.getUSTStudentWalletByEmail(principal.getName()).get(0);
			if (wallet != null) {
				model.addAttribute("walletAmt", wallet.getCurrBalance());
			} else {
				model.addAttribute("walletAmt", "NaN");
			}
			model.addAttribute("user", principal.getName());
			model.addAttribute("right","STUDENT");
			model.addAttribute("avaliableSoccerSize", lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule().size());
			model.addAttribute("youreventsize", pssusBookingMapper.getMyPSSUSBookingRecordByEmail(principal.getName()).size());
		} else if(StringUtils.equals(ustUser.getRight(),"ADMIN")) {
			model.addAttribute("user",principal.getName());
			model.addAttribute("right","ADMIN");
			model.addAttribute("pendingRecordSize", pssusBookingMapper.getPendingPSSUSBookingRecord().size());
			model.addAttribute("avaliableSoccerSize", lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule().size());
		}
		return "greeting";
	}

	@RequestMapping(value="/getRecentAvaSch")
	public @ResponseBody List<LCSDSoccerPitchSchedule> getRecentAvaSch(Model model) {
		return lcsdSoccerPitchScheduleMapper.getAvaliableLCSDSoccerPitchSchedule();
	}

	@GetMapping({"","/login"})
	public String login(Model model, @RequestParam(required = false) String error) {
		model.addAttribute("user", new USTUser());
        if(error != null &&
				error.equals(String.valueOf(HttpStatus.UNAUTHORIZED.value()))) {
			model.addAttribute("error",  HttpStatus.UNAUTHORIZED.value() + " : Bad Credentials");
		}
		return "index";
	}
}
