package com.hkust.isom5260.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LCSDDistrict {

    @SerializedName("District_Id")
    private String id;

    @SerializedName("District_Name_EN")
    private String district_name_en;

    @SerializedName("District_Name_TC")
    private String district_name_tc;

    @SerializedName("District_SHORTFORM")
    private String district_shortform;
}
