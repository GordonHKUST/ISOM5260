package com.hkust.isom5260.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class USTUser {

    private Long id;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String studentId;

    private String program;

    private String studyYear;

    private String phone;

    private String right;

}
