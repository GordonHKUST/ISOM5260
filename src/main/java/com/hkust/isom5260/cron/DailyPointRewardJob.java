package com.hkust.isom5260.cron;

import com.hkust.isom5260.mapper.PSSUSBookingMapper;
import com.hkust.isom5260.mapper.PSSUSUserMapper;
import com.hkust.isom5260.model.USTStudentWallet;
import com.hkust.isom5260.model.USTStudentWalletTransaction;
import com.hkust.isom5260.model.USTUser;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Component
public class DailyPointRewardJob {

    private static final Properties properties = new Properties();

    @Autowired
    private PSSUSUserMapper pssusUserMapper;

    @Autowired
    private PSSUSBookingMapper pssusBookingMapper;

    private void loadProperties() {
        String propertiesFilePath = "src/main/resources/application.properties";
        try (InputStream input = Files.newInputStream(Paths.get(propertiesFilePath))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }
    public DailyPointRewardJob() {
        loadProperties();
    }

    // Simple job for assign 20 credit points to users everyday
    @Scheduled(cron = "0 0 0 * * *")
    protected void executeInternal() throws JobExecutionException {

        if(properties.getProperty("job.enabled.backend").equals("false")) {
            return;
        }

        System.out.println("PointGivenJob started");
        List<USTUser> ustUser = pssusUserMapper.selectAllStudent();
        for (USTUser user : ustUser) {
            System.out.println("user : " + user.getEmail() + " is update point every day ");
            USTStudentWallet wallet = pssusBookingMapper.getUSTStudentWalletByEmail(user.getEmail()).get(0);
            double currBalance = wallet.getCurrBalance();
            currBalance += 20;
            wallet.setCurrBalance(currBalance);
            System.out.println("user : " + user.getEmail() + " is wallet updated ");
            pssusBookingMapper.updateWallet(wallet);
            USTStudentWalletTransaction walletTransaction = getUstStudentWalletTransaction(wallet);
            pssusBookingMapper.insertStudentWalletTransaction(walletTransaction);
            System.out.println("user : " + user.getEmail() + " is wallet transaction updated ");
            System.out.println("user : " + user.getEmail() + " is end updated ");
        }
        System.out.println("PointGivenJob ended");
    }

    private static USTStudentWalletTransaction getUstStudentWalletTransaction(USTStudentWallet wallet) {
        USTStudentWalletTransaction walletTransaction = new USTStudentWalletTransaction();
        walletTransaction.setAmount(20);
        walletTransaction.setTransactionLog("Daily Rewards : 20 Points");
        Date transactionDate = new Date(); // Example transaction date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(transactionDate);
        walletTransaction.setTxn_date(dateString);
        walletTransaction.setAction_name("CR");
        walletTransaction.setWalletId(wallet.getWallet_id());
        return walletTransaction;
    }
}
