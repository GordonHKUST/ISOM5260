package com.hkust.isom5260.cron;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkust.isom5260.model.LCSDSoccerPitchSchedule;
import com.hkust.isom5260.model.PSSUSBookingRecord;
import com.hkust.isom5260.mapper.LCSDSoccerPitchScheduleMapper;
import com.hkust.isom5260.mapper.PSSUSBookingMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

@Component
public class SyncLCSDSoccerPitchCronJob {

    @Autowired
    private LCSDSoccerPitchScheduleMapper soccerPitchScheduleMapper;

    @Autowired
    private PSSUSBookingMapper pssusBookingMapper;

    private static final Properties properties = new Properties();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public SyncLCSDSoccerPitchCronJob() {
        loadProperties();
    }

    private void loadProperties() {
        String propertiesFilePath = "src/main/resources/application.properties";
        try (InputStream input = Files.newInputStream(Paths.get(propertiesFilePath))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    protected void executeInternal() throws JobExecutionException {
        if(properties.getProperty("job.enabled.backend").equals("false")) {
            return;
        }

        System.out.println("Job executed at: " + System.currentTimeMillis());
        String jsonResponse = fetchJsonData(properties.getProperty("lcsd.soccer.pitch.api.url"));

        if (jsonResponse == null) {
            return;
        }

        List<LCSDSoccerPitchSchedule> soccerPitches = parseJsonData(jsonResponse);
        syncSoccerPitchesSchedule(soccerPitches);
        performHousekeeping();
    }

    private String fetchJsonData(String urlString) {
        System.out.println("Fetch Json from LCSD started");
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("Fetch Json from LCSD ended");
        return jsonBuilder.toString();
    }

    private List<LCSDSoccerPitchSchedule> parseJsonData(String jsonData) {
        Gson gson = new Gson();
        return gson.fromJson(jsonData, new TypeToken<List<LCSDSoccerPitchSchedule>>() {}.getType());
    }

    private void syncSoccerPitchesSchedule(List<LCSDSoccerPitchSchedule> soccerPitches) {
        System.out.println("Sync Soccer Pitch Schedule started");
        for (LCSDSoccerPitchSchedule soccerPitch : soccerPitches) {
            List<LCSDSoccerPitchSchedule> existingSchedules = soccerPitchScheduleMapper.getExistedSchedule(soccerPitch);
            if (isScheduleUnavailable(existingSchedules)) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(existingSchedules)) {
                updateSchedule(soccerPitch);
            } else {
                insertSchedule(soccerPitch);
            }
        }
        System.out.println("Sync Soccer Pitch Schedule Ended");
    }

    private boolean isScheduleUnavailable(List<LCSDSoccerPitchSchedule> existingSchedules) {
        return CollectionUtils.isNotEmpty(existingSchedules) &&
                (!StringUtils.equals(existingSchedules.get(0).getStatus_code(), "AVALIABLE") ||
                        StringUtils.isEmpty(existingSchedules.get(0).getStatus_code()));
    }

    private void logScheduleSkipped(LCSDSoccerPitchSchedule soccerPitch) {
        System.out.println("Schedule skipped: " + formatSchedule(soccerPitch) + " (not available)");
    }

    private void updateSchedule(LCSDSoccerPitchSchedule soccerPitch) {
        reviewStsCode(soccerPitch);
        System.out.println("Schedule updated: " + formatSchedule(soccerPitch));
        soccerPitchScheduleMapper.update(soccerPitch);
    }

    private void insertSchedule(LCSDSoccerPitchSchedule soccerPitch) {
        reviewStsCode(soccerPitch);
        System.out.println("Schedule inserted: " + formatSchedule(soccerPitch));
        soccerPitchScheduleMapper.insert(soccerPitch);
    }

    private void reviewStsCode(LCSDSoccerPitchSchedule soccerPitch) {
        if (StringUtils.equals(soccerPitch.getAvailable_courts(), "0")) {
            soccerPitch.setStatus_code("FULL_BOOKING");
        } else {
            soccerPitch.setStatus_code("AVALIABLE");
        }
    }

    private String formatSchedule(LCSDSoccerPitchSchedule soccerPitch) {
        return String.format("%s %s %s - %s",
                soccerPitch.getVenue_name_en(),
                soccerPitch.getAvailable_date(),
                soccerPitch.getSession_start_time(),
                soccerPitch.getSession_end_time());
    }

    private void performHousekeeping() {
        System.out.println("Housekeep Soccer Pitch Schedule started");
        List<LCSDSoccerPitchSchedule> houseKeepSchedules = soccerPitchScheduleMapper.getHouseKeepLCSDSoccerPitchSchedule();
        for (LCSDSoccerPitchSchedule soccerPitch : houseKeepSchedules) {
            runHouseKeep(soccerPitch);
        }
        System.out.println("Housekeep Soccer Pitch Schedule ended");
    }

    private void runHouseKeep(LCSDSoccerPitchSchedule soccerPitch) {
        try {
            LocalDateTime inputDateTime = parseScheduleTime(soccerPitch);
            if (inputDateTime.isBefore(LocalDateTime.now())) {
                soccerPitch.setAvailable_courts("0");
                soccerPitch.setStatus_code("TIME_SLOT_STARTED");
                List<PSSUSBookingRecord> bookingRecords = pssusBookingMapper.getPSSUSBookingRecordByScheduleId(soccerPitch.getId());
                for(PSSUSBookingRecord bookingRecord : bookingRecords) {
                    System.out.println("bookingRecords ID : " + bookingRecord.getBooking_Id() + ", Soccer Pitch Id: "
                            + soccerPitch.getId() + " had been found");
                    if(StringUtils.equals(bookingRecord.getStatus_code(),"PENDING_APPROVAL")) {
                        bookingRecord.setStatus_code("TIME_SLOT_STARTED_AND_NOT_APPROVED");
                    } else if(StringUtils.equals(bookingRecord.getStatus_code(),"APPROVED_AND_REGISTERED")) {
                        bookingRecord.setStatus_code("TIME_SLOT_STARTED_AND_APPROVED");
                    }
                    pssusBookingMapper.adminBookingRecordAction(bookingRecord);
                    System.out.println("Booking Pitch Schedule ID :" + bookingRecord.getSchedule_id() +
                            " had updated to " + bookingRecord.getStatus_code());
                }
            } else {
                soccerPitch.setStatus_code("AVALIABLE");
            }
            soccerPitchScheduleMapper.update(soccerPitch);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private LocalDateTime parseScheduleTime(LCSDSoccerPitchSchedule soccerPitch) {
        LocalDate inputDate = LocalDate.parse(soccerPitch.getAvailable_date(), DATE_FORMATTER);
        LocalTime inputTime = LocalTime.parse(soccerPitch.getSession_start_time(), TIME_FORMATTER);
        return LocalDateTime.of(inputDate, inputTime);
    }
}