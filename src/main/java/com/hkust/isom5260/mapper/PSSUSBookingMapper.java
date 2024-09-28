package com.hkust.isom5260.mapper;

import com.hkust.isom5260.model.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PSSUSBookingMapper {
    @Insert("INSERT INTO PSSUS_Booking_Record " +
            "(booking_Id, student_Id, campaign_Detail, campaign_remark, " +
            "join_person, email, phone, personal_trainer, schedule_id, status_code ,record_create_date) " +
            "VALUES " +
            "(booking_id_seq.NEXTVAL, #{student_Id}, #{campaign_Detail}, #{campaign_Remark}, " +
            "#{join_person}, #{email}, #{phone}, #{personal_trainer}, #{schedule_id} , #{status_code} , #{record_create_date})")
    void insertBookingRecord(PSSUSBookingRecord bookingRecord);

    @Insert("INSERT INTO UST_Student_Wallet_Transaction(TRANSACTIONID,WALLET_ID,AMOUNT,TRANSACTIONLOG,BOOKING_ID,TXN_DATE ,ACTION_NAME)" +
            "VALUES " +
            "(booking_txn_seq.NEXTVAL, #{walletId}, #{amount}, #{transactionLog} , #{booking_id} , #{txn_date} ,#{action_name} )")
    void insertStudentWalletTransaction(USTStudentWalletTransaction transaction);

    @Insert("INSERT INTO PSSUS_JOIN_Booking_Record(JOIN_ID,BOOKING_RECORD_ID,JOINER_STUDENT_ID,JOINER_EMAIL,RECORD_CREATE_DATE)" +
            "VALUES " +
            "(booking_join_seq.NEXTVAL,#{booking_record_id},#{joiner_student_id},#{joiner_email} , #{record_create_date} )")
    void insertJoinRecord(PSSUSJoinBookingRecord bookingRecord);

    @Update("UPDATE PSSUS_Booking_Record SET status_code = #{status_code, jdbcType=VARCHAR} where booking_Id = #{booking_Id, jdbcType=INTEGER} ")
    void updateStsCode(PSSUSBookingRecord bookingRecord);

    @Update("UPDATE UST_STUDENT_WALLET SET currBalance = #{currBalance} where wallet_id = #{wallet_id} ")
    void updateWallet(USTStudentWallet ustStudentWallet);

    @Select("SELECT * from PSSUS_Booking_Record pbr " +
            "LEFT JOIN PSSUS_JOIN_BOOKING_RECORD jbr " +
            "ON pbr.booking_id = jbr.booking_record_id " +
            "WHERE pbr.email = #{email} OR jbr.joiner_email = #{email} ")
    List<PSSUSBookingRecord> getMyPSSUSBookingRecordByEmail(String email);

    @Select("SELECT DISTINCT *\n" +
            "FROM PSSUS_Booking_Record pbr\n" +
            "LEFT JOIN PSSUS_JOIN_BOOKING_RECORD jbr ON pbr.booking_id = jbr.booking_record_id\n" +
            "WHERE status_code = 'PENDING_APPROVAL'\n" +
            "AND pbr.email != #{email}\n" +
            "AND NOT EXISTS (\n" +
            "    SELECT 1\n" +
            "    FROM PSSUS_JOIN_BOOKING_RECORD jbr2\n" +
            "    WHERE jbr2.joiner_email = #{email}\n" +
            "    AND jbr2.booking_record_id = pbr.booking_id\n" +
            ")")
    List<PSSUSBookingRecord> getOtherActivePSSUSBookingRecord(String email);

    @Select("SELECT * from UST_STUDENT_WALLET WHERE email = #{email}")
    List<USTStudentWallet> getUSTStudentWalletByEmail(String email);

    @Select("SELECT * from UST_STUDENT_WALLET_TRANSACTION WHERE WALLET_ID = #{wallet_id}")
    List<USTStudentWalletTransaction> getUSTStudentWalletTxnByWalletId(int wallet_id);

    @Select("SELECT * from PSSUS_Booking_Record WHERE booking_Id = #{booking_Id}")
    List<PSSUSBookingRecord> getPSSUSBookingRecordByBookingId(String booking_Id);

    @Select("SELECT count(1) from PSSUS_JOIN_BOOKING_RECORD WHERE BOOKING_RECORD_ID = #{booking_Id} AND JOINER_EMAIL = #{joiner_email}")
    int getCountPSSUSBookingRecordByBookingId(@Param("booking_Id") String booking_Id, @Param("joiner_email")String joiner_email);

    @Select("Select * from PSSUS_Booking_Record WHERE schedule_Id = #{schedule_Id}")
    List<PSSUSBookingRecord> getPSSUSBookingRecordByScheduleId(String schedule_Id);

    @Select("Select * from PSSUS_Booking_Record WHERE STATUS_CODE = 'PENDING_APPROVAL'")
    List<PSSUSBookingRecord> getPendingPSSUSBookingRecord();

    @Select("Select * from LCSD_SOCCER_PITCH WHERE PITCHTYPE = #{pitchType}")
    List<LCSDSoccerPitch> getLCSDSoccerPitchByPitchType(String pitchType);
}
