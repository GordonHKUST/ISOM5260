package com.hkust.isom5260.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LCSDActivity {
    // Getters and setters
    @SerializedName("PGM_CODE")
    private String programCode;
    @SerializedName("TC_PGM_NAME")
    private String tcProgramName;
    @SerializedName("EN_PGM_NAME")
    private String enProgramName;
    // Add other fields as necessary

    // Override toString for easy printing
    @Override
    public String toString() {
        return "Program{" +
                "programCode='" + programCode + '\'' +
                ", tcProgramName='" + tcProgramName + '\'' +
                ", enProgramName='" + enProgramName + '\'' +
                '}';
    }
}