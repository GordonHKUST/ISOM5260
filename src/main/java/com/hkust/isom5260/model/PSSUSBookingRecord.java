package com.hkust.isom5260.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PSSUSBookingRecord {
    private String booking_Id;
    private String student_Id;
    private String campaign_Detail;
    private String campaign_Remark;
    private String join_person;
    private String email;
    private String phone;
    private String personal_trainer;
    private String schedule_id;
    private String status_code;
    private String session_start_time;
    private String session_end_time;
    private String available_date;
    private String venue_Name_En;
    private String last_Name;
    private String first_Name;
    private String record_create_date;
}
