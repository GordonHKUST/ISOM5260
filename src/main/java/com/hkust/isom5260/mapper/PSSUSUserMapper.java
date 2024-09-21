package com.hkust.isom5260.mapper;

import com.hkust.isom5260.model.USTStudent;
import com.hkust.isom5260.model.USTStudentWallet;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PSSUSUserMapper {

    @Insert("insert into USTStudent(id, email , password ,firstName , lastName , studentId , phone , studyYear ) " +
            "values (uststd_seq.nextval, #{email, jdbcType=VARCHAR}, #{password, jdbcType=VARCHAR}, #{firstName, jdbcType=VARCHAR} , #{lastName, jdbcType=VARCHAR} , #{studentId, jdbcType=VARCHAR}, #{phone, jdbcType=VARCHAR} , #{studyYear, jdbcType=VARCHAR} )")
    void insert (USTStudent usr);

    @Insert("INSERT INTO UST_Student_Wallet (walletId, email, currBalance, lastMonthBalanceLeft) " +
            "VALUES (wallet_id_seq.NEXTVAL, #{email}, #{currBalance}, #{lastMonthBalanceLeft})")
    void insert_wallet (USTStudentWallet usr);

    @Select("select * from USTStudent where email = #{email}")
    USTStudent selectByEmail(String email);

}
