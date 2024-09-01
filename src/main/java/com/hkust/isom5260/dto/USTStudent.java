package com.hkust.isom5260.dto;

import lombok.Getter;

@Getter
public class USTStudent {

    private Long id;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



}
