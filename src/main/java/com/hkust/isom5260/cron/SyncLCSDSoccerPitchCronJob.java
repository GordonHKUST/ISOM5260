package com.hkust.isom5260.cron;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkust.isom5260.dto.LCSDSoccerPitchSchedule;
import com.hkust.isom5260.mapper.LCSDSoccerPitchScheduleMapper;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Component
public class SyncLCSDSoccerPitchCronJob {

    @Autowired
    private LCSDSoccerPitchScheduleMapper soccerPitchScheduleMapper;

    private static Properties properties = new Properties();

    public SyncLCSDSoccerPitchCronJob() {
        String propertiesFilePath = "src/main/resources/application.properties";
        try (InputStream input = Files.newInputStream(Paths.get(propertiesFilePath))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "* * 1 * * ?")
    protected void executeInternal() throws JobExecutionException {
        System.out.println("Job executed at: " + System.currentTimeMillis());
        String urlString = properties.getProperty("lcsd.soccer.pitch.api.url");
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
        List<LCSDSoccerPitchSchedule> soccerPitches = gson.fromJson(jsonBuilder.toString(), new TypeToken<List<LCSDSoccerPitchSchedule>>(){}.getType());
        for(LCSDSoccerPitchSchedule soccerPitch : soccerPitches) {
            if(soccerPitchScheduleMapper.checkVenueExists(soccerPitch) > 0) {
                soccerPitchScheduleMapper.updateVenueAvaliableCount(soccerPitch);
            } else {
                soccerPitchScheduleMapper.insert(soccerPitch);
            }
        }
    }
}

