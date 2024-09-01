package com.hkust.isom5260;

import java.security.Principal;
import java.util.List;

import com.hkust.isom5260.dto.USTStudent;
import com.hkust.isom5260.mapper.CarMapper;
import com.hkust.isom5260.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AppController {

    @Autowired
	private CarMapper carMapper;

    @Autowired
	private UserMapper userMapper;
	
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new USTStudent());
		return "signup_form";
	}
	
	@PostMapping("/process_register")
	public String processRegister(USTStudent USTStudent) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(USTStudent.getPassword());
		USTStudent.setPassword(encodedPassword);
		userMapper.insert(USTStudent);
		return "register_success";
	}
	
	@GetMapping("/users")
	public String listUsers(Model model, Principal principal) {
		List<USTStudent> listUSTStudents = userMapper.selectAll();
		model.addAttribute("listUsers", listUSTStudents);
		return "users";
	}
}
