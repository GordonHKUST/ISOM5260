package com.hkust.isom5260.mapper;

import com.hkust.isom5260.model.USTUser;
import com.hkust.isom5260.model.USTStudentWallet;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PSSUSUserMapper {

    @Insert("insert into USTUser(id, email , password ,firstName , lastName , studentId , phone , studyYear , right , program ) " +
            "values (uststd_seq.nextval, #{email, jdbcType=VARCHAR}, #{password, jdbcType=VARCHAR}, #{firstName, jdbcType=VARCHAR} , #{lastName, jdbcType=VARCHAR} , #{studentId, jdbcType=VARCHAR}, #{phone, jdbcType=VARCHAR} , #{studyYear, jdbcType=VARCHAR} , #{ right, jdbcType=VARCHAR}  , #{program, jdbcType=VARCHAR})")
    void insert (USTUser usr);

    @Insert("INSERT INTO UST_Student_Wallet (wallet_id, email, currBalance, lastMonthBalanceLeft) " +
            "VALUES (wallet_id_seq.NEXTVAL, #{email}, #{currBalance}, #{lastMonthBalanceLeft})")
    void insert_wallet (USTStudentWallet usr);

    @Select("select * from USTUser where email = #{email}")
    USTUser selectByEmail(String email);

}
