package com.hkust.isom5260.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USTStudentWalletTransaction {

    private int walletId;

    private int transactionId;

    private double amount;

    private String transactionLog;

    private int booking_id;

    private String txn_date;

    private String action_name;
}
