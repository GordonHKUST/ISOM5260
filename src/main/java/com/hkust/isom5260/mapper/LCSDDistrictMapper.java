package com.hkust.isom5260.mapper;

import com.hkust.isom5260.model.LCSDDistrict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LCSDDistrictMapper {

    @Select("SELECT *\n" +
            "FROM LCSD_DISTRICT\n" +
            "WHERE district_name_en IN (\n" +
            "    SELECT s.district_name_en\n" +
            "    FROM LCSD_SOCCER_PITCH_SCHEDULE s\n" +
            "    GROUP BY s.district_name_en\n" +
            ")")
    List<LCSDDistrict> selectDistrictFromSchedule();

}
