package com.hkust.isom5260.mapper;

import com.hkust.isom5260.model.USTUser;
import com.hkust.isom5260.model.USTStudentWallet;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PSSUSUserMapper {

    @Insert("insert into USTUser(email , password ,firstName , lastName , studentId , phone , studyYear , right , program ) " +
            "values (#{email, jdbcType=VARCHAR}, #{password, jdbcType=VARCHAR}, #{firstName, jdbcType=VARCHAR} , #{lastName, jdbcType=VARCHAR} , #{studentId, jdbcType=VARCHAR}, #{phone, jdbcType=VARCHAR} , #{studyYear, jdbcType=VARCHAR} , #{ right, jdbcType=VARCHAR}  , #{program, jdbcType=VARCHAR})")
    void insert (USTUser usr);

    @Insert("INSERT INTO UST_Student_Wallet (wallet_id, email, currBalance, lastMonthBalanceLeft) " +
            "VALUES (wallet_id_seq.NEXTVAL, #{email}, #{currBalance}, #{lastMonthBalanceLeft})")
    void insert_wallet (USTStudentWallet usr);

    @Select("select * from USTUser where email = #{email}")
    USTUser selectByEmail(String email);

    @Select("select * from USTUser where STUDENTID = #{student_id}")
    USTUser selectByStudentId(String student_id);

    @Select("select * from USTUser WHERE right = 'STUDENT'")
    List<USTUser> selectAllStudent();

}
