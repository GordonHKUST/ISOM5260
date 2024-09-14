package com.hkust.isom5260.mapper;

import com.hkust.isom5260.dto.LCSDSoccerPitchSchedule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LCSDSoccerPitchScheduleMapper {
    @Insert("INSERT INTO LCSD_SOCCER_PITCH_SCHEDULE (\n" +
            "    id\n," +
            "    district_name_en,\n" +
            "    district_name_tc,\n" +
            "    venue_name_en,\n" +
            "    venue_name_tc,\n" +
            "    venue_address_en,\n" +
            "    venue_address_tc,\n" +
            "    venue_phone_no,\n" +
            "    venue_longitude,\n" +
            "    venue_latitude,\n" +
            "    facility_type_name_en,\n" +
            "    facility_type_name_tc,\n" +
            "    facility_location_name_en,\n" +
            "    facility_location_name_tc,\n" +
            "    available_date,\n" +
            "    session_start_time,\n" +
            "    session_end_time,\n" +
            "    available_courts\n" +
            ") VALUES (\n" +
            "    schedule_seq.NEXTVAL, \n"+
            "    #{district_name_en, jdbcType=VARCHAR},\n" +
            "    #{district_name_tc, jdbcType=NVARCHAR},\n" +
            "    #{venue_name_en, jdbcType=VARCHAR},\n" +
            "    #{venue_name_tc, jdbcType=NVARCHAR},\n" +
            "    #{venue_address_en, jdbcType=VARCHAR},\n" +
            "    #{venue_address_tc, jdbcType=VARCHAR},\n" +
            "    #{venue_phone_no, jdbcType=VARCHAR},\n" +
            "    #{venue_longitude, jdbcType=VARCHAR},\n" +
            "    #{venue_latitude, jdbcType=VARCHAR},\n" +
            "    #{facility_type_name_en, jdbcType=VARCHAR},\n" +
            "    #{facility_type_name_tc, jdbcType=NVARCHAR},\n" +
            "    #{facility_location_name_en, jdbcType=VARCHAR},\n" +
            "    #{facility_location_name_tc, jdbcType=NVARCHAR},\n" +
            "    #{available_date, jdbcType=VARCHAR},\n" +
            "    #{session_start_time, jdbcType=VARCHAR},\n" +
            "    #{session_end_time, jdbcType=VARCHAR},\n" +
            "    #{available_courts, jdbcType=INTEGER}\n" +
            ")")
    void insert (LCSDSoccerPitchSchedule soccerPitch);

    @Select("        SELECT COUNT(*) \n" +
            "        FROM LCSD_SOCCER_PITCH_SCHEDULE \n" +
            "        WHERE venue_name_en = #{venue_name_en} \n" +
            "          AND available_date = #{available_date} \n" +
            "          AND session_start_time = #{session_start_time}")
    int checkVenueExists(LCSDSoccerPitchSchedule soccerPitch);

    @Update(" UPDATE LCSD_SOCCER_PITCH_SCHEDULE\n" +
            " SET available_courts = #{available_courts , jdbcType=INTEGER}\n" +
            " WHERE venue_name_en = #{venue_name_en} AND available_date = #{available_date} AND session_start_time = #{session_start_time}")
    void updateVenueAvaliableCount(LCSDSoccerPitchSchedule soccerPitch);

    @Select("Select * from LCSD_SOCCER_PITCH_SCHEDULE WHERE id = #{id}")
    List<LCSDSoccerPitchSchedule> getLCSDSoccerPitchScheduleById(int id);

    @Select("    SELECT LCSD_SOCCER_PITCH_SCHEDULE.*\n" +
            "    FROM LCSD_SOCCER_PITCH_SCHEDULE\n" +
            "    WHERE available_courts > 0")
    List<LCSDSoccerPitchSchedule> getAvaliableLCSDSoccerPitchSchedule();
}