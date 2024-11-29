package newREALs.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Report;
import newREALs.backend.repository.ReportRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportSaveService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    // 레포트 생성
    @Transactional
    @Scheduled(cron = "0 14 11 29 * ?")
    public void makeReports() throws JsonProcessingException {
        List<Accounts> users = userRepository.findAll();
        for(Accounts user : users) {
            Map<String, Object> reportData = reportService.generateReportData(user.getId());
            String jsonData = objectMapper.writeValueAsString(reportData);

            Report report = reportRepository.findByUserId(user.getId())
                    .orElse(null);
            if(report != null) {
                reportRepository.updateReport(user.getId(), jsonData);
            }
            else {
                report = new Report();
                report.setUser(user);
                report.setReport(jsonData);
            }
            reportRepository.save(report);
        }
    }

}
