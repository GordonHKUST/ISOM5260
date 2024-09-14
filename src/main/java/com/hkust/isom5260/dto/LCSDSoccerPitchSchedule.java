package com.hkust.isom5260.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class LCSDSoccerPitchSchedule {
    @SerializedName("Id")
    private String id;

    @SerializedName("District_Name_EN")
    private String district_name_en;

    @SerializedName("District_Name_TC")
    private String district_name_tc;

    @SerializedName("Venue_Name_EN")
    private String venue_name_en;

    @SerializedName("Venue_Name_TC")
    private String venue_name_tc;

    @SerializedName("Venue_Address_EN")
    private String venue_address_en;

    @SerializedName("Venue_Address_TC")
    private String venue_address_tc;

    @SerializedName("Venue_Phone_No.")
    private String venue_phone_no;

    @SerializedName("Venue_Longitude")
    private String venue_longitude;

    @SerializedName("Venue_Latitude")
    private String venue_latitude;

    @SerializedName("Facility_Type_Name_EN")
    private String facility_type_name_en;

    @SerializedName("Facility_Type_Name_TC")
    private String facility_type_name_tc;

    @SerializedName("Facility_Location_Name_EN")
    private String facility_location_name_en;

    @SerializedName("Facility_Location_Name_TC")
    private String facility_location_name_tc;

    @SerializedName("Available_Date")
    private String available_date;

    @SerializedName("Session_Start_Time")
    private String session_start_time;

    @SerializedName("Session_End_Time")
    private String session_end_time;

    @SerializedName("Available_Courts")
    private String available_courts;
}