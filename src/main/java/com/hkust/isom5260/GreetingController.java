package com.hkust.isom5260;

import com.hkust.isom5260.dto.Car;
import com.hkust.isom5260.dto.USTStudent;
import com.hkust.isom5260.mapper.CarMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class GreetingController {

    @Autowired
    private CarMapper carMapper;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        Collection<Car> cars = carMapper.selectAll();
        model.addAttribute("cars",cars);
        System.out.println("--------------------");
        return "greeting";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody USTStudent USTStudent) {
        return ResponseEntity.ok("User registered successfully");
    }

    @RequestMapping(value="/gettodos")
    public @ResponseBody List<Car> getTodos(Model model) {
        return (List<Car>)carMapper.selectAll();
    }
}
