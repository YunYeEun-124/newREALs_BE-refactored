package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {
    private final JobLauncher jobLauncher;
    private final Job userReportJob;
    @Scheduled(cron = "0 0 1 1 * ?")  // 매달 1일 01:00에 실행
    public void runMonthlyReportJob() {
        try {
            jobLauncher.run(userReportJob, new org.springframework.batch.core.JobParameters());
            System.out.println("월간 보고서 생성 작업 완료");
        } catch (Exception e) {
            System.err.println("배치 작업 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
