package com.example.stockservice.config;

import com.example.stockservice.service.StockService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private final JobLauncher jobLauncher;
    private final Job dailyJob;
    private final StockService stockService;

    public SchedulerConfig(JobLauncher jobLauncher, Job dailyJob, StockService stockService) {
        this.jobLauncher = jobLauncher;
        this.dailyJob = dailyJob;
        this.stockService = stockService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 00시에 실행
    public void performDailyJob() throws Exception {
        jobLauncher.run(dailyJob, new JobParametersBuilder().toJobParameters());
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI") // 거래 시간 동안 매 1분마다 실행 (09:00 ~ 15:30)
    public void performRealTimeDataCollection() {
        stockService.collectAndSaveRealTimeData();
    }
}
