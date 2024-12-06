package newREALs.backend.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobBuilderHelper;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job userReportJob(Step userReportStep) {
        return new JobBuilder("userReportJob", jobRepository)
                .start(userReportStep)
                .build();
    }

    @Bean
    public Step userReportStep(Tasklet userReportTasklet) {
        return new StepBuilder("userReportStep", jobRepository)
                .tasklet(userReportTasklet, transactionManager)
                .build();
    }
}
