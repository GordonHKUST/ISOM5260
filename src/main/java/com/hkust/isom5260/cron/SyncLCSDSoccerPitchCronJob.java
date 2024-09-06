package com.hkust.isom5260.cron;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkust.isom5260.dto.LCSDActivity;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component
public class SyncLCSDSoccerPitchCronJob {
    //Https://www.smartplay.lcsd.gov.hk/rest/cms/api/v1/publ/contents/open-data/turf-soccer-pitch/file
    @Scheduled(cron = "* * 1 * * ?")
    protected void executeInternal() throws JobExecutionException {
        System.out.println("Job executed at: " + System.currentTimeMillis());
        String urlString = "https://www.smartplay.lcsd.gov.hk/rest/cms/api/v1/publ/contents/open-data/turf-soccer-pitch/file";
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                jsonBuilder.append(inputLine);
            }
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        List<LCSDActivity> programs = gson.fromJson(jsonBuilder.toString(), new TypeToken<List<LCSDActivity>>(){}.getType());
        for (LCSDActivity program : programs) {
            System.out.println(program);
        }
    }
}

