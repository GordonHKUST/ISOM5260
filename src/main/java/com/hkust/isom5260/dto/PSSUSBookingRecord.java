package com.hkust.isom5260.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PSSUSBookingRecord {
    @SerializedName("booking_Id")
    private String booking_Id;
    @SerializedName("student_Id")
    private String student_Id;
    @SerializedName("campaign_Detail")
    private String campaign_Detail;
    @SerializedName("campaign_Remark")
    private String campaign_Remark;
    @SerializedName("join_person")
    private String join_Person;
    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("personal_trainer")
    private String personal_Trainer;
    @SerializedName("schedule_id")
    private String schedule_Id;
    @SerializedName("status_code")
    private String status_code;

    private String session_start_time;
    private String session_end_time;
    private String available_date;
    private String venue_Name_En;
}
