package com.hkust.isom5260.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String hashPassword(String password) {
        //return "";
        return passwordEncoder.encode(password);
    }
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        //return false;
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}