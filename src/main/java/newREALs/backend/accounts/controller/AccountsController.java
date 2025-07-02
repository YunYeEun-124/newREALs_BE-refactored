package newREALs.backend.accounts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.common.dto.ApiResponseDTO;
import newREALs.backend.accounts.repository.UserKeywordRepository;
import newREALs.backend.accounts.repository.UserRepository;
import newREALs.backend.accounts.service.AttendanceService;
import newREALs.backend.common.service.KakaoService;
import newREALs.backend.accounts.service.ProfileService;
import newREALs.backend.common.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final AttendanceService attendanceService;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // 이렇게 해야 줄바꿈됨
    private final UserKeywordRepository userKeywordRepository;

    @GetMapping("/attendance/check")
    public ResponseEntity<?> Checkattendance(HttpServletRequest userInfo){
        Long userid = tokenService.getUserId(userInfo);
        LocalDateTime current = LocalDateTime.now();
        int day = current.getDayOfMonth();
        Map<String, Object> response = new LinkedHashMap<>();
        //0~30까지 [0] = 1일, [1] = 2일 .. , [30] = 31일

        boolean attendance = attendanceService.GetAttendance(userid);

        response.put("day",(day));
        response.put("attendance",attendance);

        if(attendance){
            return ResponseEntity.ok(
                    ApiResponseDTO.success( (day)+"일 출석 했습니다. ",response)
            );

        }else {
            return ResponseEntity.ok(
                    ApiResponseDTO.success( (day)+"일 출석 하지 않았습니다.",response)
            );
        }
    }
    //[patch] 출석 체크 버튼 누르기
    @PatchMapping("/attendance/mark")
    public ResponseEntity<?> Markattendance(HttpServletRequest userInfo){

        Long userid = tokenService.getUserId(userInfo);
        if(userid == null) {
            throw new SecurityException("유효하지 않은 토큰입니다");
        }
        int day = attendanceService.UpdateAttendance(userid);
        
        if(day != -1 ){
            return ResponseEntity.ok(
                    ApiResponseDTO.success( (day+1)+"일 출석 체크 성공 ",null)
            );

        }

        throw new IllegalStateException("출석 체크 실패 ");

    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<?>> kakaoLogin(@RequestParam Map<String, String> request) {
        String authorizationCode = request.get("code");
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new IllegalArgumentException("인가코드가 비어있습니다.");
        }

        // 카카오 로그인 처리
        Map<String, Object> kakaoResponse = kakaoService.processKakaoLogin(authorizationCode);
        boolean isNewAccount = (boolean) kakaoResponse.get("isNewAccount");

        // 리다이렉트 URL 결정
        String redirectUrl;
        if (isNewAccount) {
            redirectUrl = "/register"; // 추가정보 입력 필요
        } else {
            redirectUrl = "/home"; // 추가정보 입력 완료
        }
        kakaoResponse.put("redirect_url", redirectUrl);

        return ResponseEntity.ok(ApiResponseDTO.success("로그인 성공", kakaoResponse));
    }


    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {

        String refreshToken = tokenService.extractTokenFromHeader(request);

        if (!tokenService.validateToken(refreshToken)) {
            return ResponseEntity.ok(ApiResponseDTO.failure("E403","유효하지 않은 Refresh Token입니다."));
        }
    
        String tokenType = tokenService.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("유효하지 않은 토큰 타입입니다.");
        }

        Long userId = tokenService.extractUserIdFromToken(refreshToken);
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));


        // access token 다시 생성
        String newAccessToken = tokenService.generateAccessToken(user);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", newAccessToken);
        return ResponseEntity.ok(ApiResponseDTO.success("Access Token 재발급 성공", responseBody));
    }

}
