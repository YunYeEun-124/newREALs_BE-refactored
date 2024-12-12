package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {
    private final AIReportService aiReportService;
    @Scheduled(cron = "59 59 23 L * ?")  // 매달 말일 23:59 실행
    public void generateMonthlyReports() {
        System.out.println("월간 보고서 생성 시작...");
        List<String> uploadedReports = aiReportService.generateReports();
        System.out.println("업로드된 보고서: " + uploadedReports);
    }
}
