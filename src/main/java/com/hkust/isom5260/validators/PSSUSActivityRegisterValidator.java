package com.hkust.isom5260.validators;

import com.hkust.isom5260.mapper.PSSUSUserMapper;
import com.hkust.isom5260.model.USTUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Component
public class PSSUSActivityRegisterValidator implements Validator {

    @Autowired
    private PSSUSUserMapper pssusUserMapper;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {

    }
}
