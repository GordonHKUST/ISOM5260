package com.hkust.isom5260.mapper;

import com.hkust.isom5260.dto.USTStudent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("insert into USTStudent(id, email , password ,firstName , lastName ) values (uststd_seq.nextval, #{email}, #{password}, #{firstName} , #{lastName})")
    void insert (USTStudent usr);

    @Select("select * from USTStudent")
    List<USTStudent> selectAll();

    @Select("select * from USTStudent where email = #{email}")
    USTStudent selectByEmail(String email);

}
