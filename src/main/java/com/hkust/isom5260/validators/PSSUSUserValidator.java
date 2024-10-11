package com.hkust.isom5260.validators;

import com.hkust.isom5260.mapper.PSSUSUserMapper;
import com.hkust.isom5260.model.USTUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
@Component
public class PSSUSUserValidator implements Validator {
    @Autowired
    private PSSUSUserMapper pssusUserMapper;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    private boolean isValid(String input) {
        return input != null && input.length() == 8 && input.matches("\\d{8}");
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(o instanceof USTUser) {
            String phone = ((USTUser) o).getPhone();
            String studentId = ((USTUser) o).getStudentId();
            if(!isValid(phone)) {
                errors.rejectValue("phone", null, "This phone number is not in 8 digits");
            }
            if(!isValid(studentId)) {
                errors.rejectValue("studentId", null, "This student id is not in 8 digits");
            }
            if(Integer.parseInt(((USTUser) o).getStudyYear()) >= 5 || Integer.parseInt(((USTUser) o).getStudyYear()) < 0) {
                errors.rejectValue("Year", null, "This study year must be smaller than 5 according to the instruction from ARRO");
            }
            USTUser existsUser = pssusUserMapper.selectByEmail(((USTUser) o).getEmail());
            if(existsUser != null) {
                errors.rejectValue("email", null, "This email is already registered");
            }
            USTUser idUser = pssusUserMapper.selectByStudentId(((USTUser) o).getStudentId());
            if(idUser != null) {
                errors.rejectValue("id", null, "This student id is already registered");
            }
        }
    }
}
