package com.hkust.isom5260.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USTStudentWalletTransaction {

    private int walletId;

    private int transactionId;

    private double amount;

    private String transactionLog;
}
