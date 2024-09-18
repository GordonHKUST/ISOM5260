package com.hkust.isom5260.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USTStudentWallet {

    private String email;

    private int walletId;

    private double currBalance;

    private double lastMonthBalanceLeft;
}
