package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.*;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/report")
public class ReportController {
    private final TokenService tokenService;
    private final ProfileService profileService;
    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping
    public ResponseEntity<?> getInterest(HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);

        Map<String, List<ReportInterestDto>> interest = profileService.getReportInterest(userId);
        Map<String, Object> change = profileService.getReportChange(userId);
        Map<String, List<ReportCompareDto>> compare = profileService.getReportCompareLast(userId);

        Map<String, List<ReportDto>> result = new HashMap<>();
//        response.put("user_id", userId);
//        response.put("interest", interest);
//        response.put("change", change);
//        response.put("compare", compare);
        result.put("report", new ArrayList<>());

        result.get("report").add(ReportDto.builder()
                        .change(change)
                        .interest(interest)
                        .compare(compare)
                .build());

        return ResponseEntity.ok(ApiResponseDTO.success("분석 레포트 - 유저 관심도 조회 성공", result));
    }
}
