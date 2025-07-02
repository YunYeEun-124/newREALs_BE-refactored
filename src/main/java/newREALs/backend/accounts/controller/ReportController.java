package newREALs.backend.accounts.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import newREALs.backend.accounts.domain.Report;
import newREALs.backend.common.service.KakaoService;
import newREALs.backend.common.dto.ApiResponseDTO;
import newREALs.backend.accounts.repository.ReportRepository;
import newREALs.backend.accounts.service.ProfileService;
import newREALs.backend.common.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/report")
public class ReportController {
    private final TokenService tokenService;
    private final ProfileService profileService;
    private final KakaoService.ReportService reportService;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping
    public ResponseEntity<?> getInterest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws JsonProcessingException {
        try {
            Long userId = tokenService.getUserId(request);
            Optional<Report> reportOptional = reportRepository.findByUserId(userId);

            if (reportOptional.isPresent()) {
                String jsonString = reportOptional.get().getReport();
                // JSON 문자열 처리
                String formattedJson = jsonString.replace("\\", "\n");
                JsonNode jsonNode = objectMapper.readTree(formattedJson);

                return ResponseEntity.ok(ApiResponseDTO.success("분석 레포트 조회 성공", jsonNode));
            } else {
                // 데이터가 없는 경우
                return ResponseEntity.ok(ApiResponseDTO.success("레포트 데이터가 없습니다", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponseDTO.failure("E401", "토큰이 유효하지 않습니다"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDTO.failure("E500", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }



    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO<?>> getReportSummary(HttpServletRequest request){
        Long userId=tokenService.getUserId(request);
        String summary=reportService.getAnalysisSummary(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("분석 요약 조회 성공", summary));
    }




}
