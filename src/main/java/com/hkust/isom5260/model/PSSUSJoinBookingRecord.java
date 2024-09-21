package com.hkust.isom5260.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PSSUSJoinBookingRecord {
   private String join_id;
   private String booking_record_id;
   private String joiner_student_id;
   private String joiner_email;
}
