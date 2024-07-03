package com.example.stockservice.config;

import com.example.stockservice.listener.JobCompletionNotificationListener;
import com.example.stockservice.service.StockService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StockService stockService;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, StockService stockService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.stockService = stockService;
    }

    @Bean
    public Job initialJob(JobCompletionNotificationListener listener, Step initialStep) {
        return new JobBuilder("initialJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(initialStep)
                .build();
    }

    @Bean
    public Step initialStep() {
        return new StepBuilder("initialStep", jobRepository)
                .tasklet(initialTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet initialTasklet() {
        return (contribution, chunkContext) -> {
            stockService.collectAndSaveInitialData();
            return RepeatStatus.FINISHED;
        };
    }
}
