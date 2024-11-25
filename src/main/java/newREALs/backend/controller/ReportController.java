package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.dto.ProfileInterestDto;
import newREALs.backend.dto.ReportInterestDto;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.ReportService;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/report")
public class ReportController {
    private final TokenService tokenService;
    private final ProfileService profileService;
    private final ReportService reportService;
    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping("/interest")
    public ResponseEntity<?> getInterest(HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);

        Map<String, List<ReportInterestDto>> interestMap = profileService.getReportInterest(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("interest", interestMap);
        return ResponseEntity.ok(ApiResponseDTO.success("분석 레포트 - 유저 관심도 조회 성공", response));
    }

    @GetMapping("/keyword")
    public ResponseEntity<ApiResponseDTO<?>> getRecommendKeyword(HttpServletRequest request){
        Long userId=tokenService.getUserId(request);
        List<String> keywords=reportService.recommendNewKeyword(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("추천 키워드 조회 성공", keywords));
    }




}
