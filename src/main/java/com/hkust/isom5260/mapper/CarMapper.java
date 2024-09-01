package com.hkust.isom5260.mapper;

import com.hkust.isom5260.dto.Car;
import org.apache.ibatis.annotations.*;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CarMapper {

    @Insert("insert into car(make, model, year, id) values (#{make}, #{model}, #{year}, car_seq.nextval)")
    void insert (Car car);

    @Select("select * from CAR")
    List<Car> selectAll();

    @Delete("delete from car where id = #{id}")
    void deletById( long id);

}

