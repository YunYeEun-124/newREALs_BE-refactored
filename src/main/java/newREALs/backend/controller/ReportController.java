package newREALs.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Report;
import newREALs.backend.dto.*;
import newREALs.backend.repository.ReportRepository;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.ReportService;
import newREALs.backend.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/report")
public class ReportController {
    private final TokenService tokenService;
    private final ProfileService profileService;
    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping
    public ResponseEntity<?> getInterest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws JsonProcessingException {
        Long userId = tokenService.getUserId(request);
        Optional<Report> reportOptional = reportRepository.findByUserId(userId);

        if(reportOptional.isPresent()) {
            // JSON 데이터를 가져옴
            String jsonString = reportOptional.get().getReport();

            // 데이터를 JSON으로 변환 후 포맷 수정
            String formattedJson = jsonString.replace("\\", "\n");

            // JSON 데이터를 파싱하여 Tree 형태로 전달
            ObjectMapper objectMapper = new ObjectMapper();
            return ResponseEntity.ok(ApiResponseDTO.success("분석 레포트 조회 성공", objectMapper.readTree(formattedJson)));
        }
        else {
            return ResponseEntity.ok(ApiResponseDTO.failure("404", "레포트 조회 실패"));
        }
    }

//    @GetMapping("/keyword")
//    public ResponseEntity<ApiResponseDTO<?>> getRecommendKeyword(HttpServletRequest request){
//        Long userId=tokenService.getUserId(request);
//        List<String> keywords=reportService.recommendNewKeyword(userId);
//        return ResponseEntity.ok(ApiResponseDTO.success("추천 키워드 조회 성공", keywords));
//    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO<?>> getReportSummary(HttpServletRequest request){
        Long userId=tokenService.getUserId(request);
        String summary=reportService.getAnalysisSummary(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("분석 요약 조회 성공", summary));
    }




}
