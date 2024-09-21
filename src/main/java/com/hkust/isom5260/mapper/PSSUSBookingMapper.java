package com.hkust.isom5260.mapper;

import com.hkust.isom5260.dto.LCSDSoccerPitchSchedule;
import com.hkust.isom5260.dto.PSSUSBookingRecord;
import com.hkust.isom5260.dto.USTStudentWallet;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PSSUSBookingMapper {
    @Insert("INSERT INTO PSSUS_Booking_Record " +
            "(booking_Id, student_Id, campaign_Detail, campaign_remark, " +
            "join_person, email, phone, personal_trainer, schedule_id, status_code) " +
            "VALUES " +
            "(booking_id_seq.NEXTVAL, #{student_Id}, #{campaign_Detail}, #{campaign_Remark}, " +
            "#{join_person}, #{email}, #{phone}, #{personal_trainer}, #{schedule_id} , #{status_code})")
    void insert(PSSUSBookingRecord bookingRecord);

    @Select("Select * from PSSUS_Booking_Record WHERE email = #{email}")
    List<PSSUSBookingRecord> getPSSUSBookingRecordByEmail(String email);

    @Select("Select * from PSSUS_Booking_Record WHERE email != #{email} " +
            "and status_code = 'PENDING_APPROVAL'")
    List<PSSUSBookingRecord> getOtherActivePSSUSBookingRecord(String email);

    @Select("Select * from UST_STUDENT_WALLET WHERE email = #{email}")
    List<USTStudentWallet> getUSTStudentWalletByEmail(String email);

    @Select("Select * from PSSUS_Booking_Record WHERE booking_Id = #{booking_Id}")
    List<PSSUSBookingRecord> getPSSUSBookingRecordByBookingId(String booking_Id);
}
