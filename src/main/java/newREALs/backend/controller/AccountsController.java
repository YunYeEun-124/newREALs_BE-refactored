package newREALs.backend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.dto.*;
import newREALs.backend.service.AttendanceService;
import newREALs.backend.service.KakaoService;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final ProfileService profileService;
    private final AttendanceService attendanceService;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // 이렇게 해야 줄바꿈됨

    //[patch] 출석 체크 버튼 누르기
    @PatchMapping("/attendance/mark")
    public ResponseEntity<?> Checkattendance(HttpServletRequest userInfo){
        try {
            Long userid = tokenService.getUserId(userInfo);
            Map<String, Object> responseBody = new HashMap<>();

            if(attendanceService.UpdateAttendance(userid)){
                responseBody.put("status", "success");
            }else {
                responseBody.put("status", "fail : already checked");
            }

            return ResponseEntity.ok().body(responseBody);

        }catch (Exception e){
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "fail");
            errorResponse.put("error", e.getMessage());

            String errorJsonResponse = gson.toJson(errorResponse);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    //[post] 유저 로그인
    @PostMapping("/login")
    public ResponseEntity<String> kakaoLogin(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("code");

        try {
            // 로그인 성공
            Map<String, Object> kakaoResponse = kakaoService.processKakaoLogin(authorizationCode);

            // 플래그로 확인
            // 여기서 바로 findByEmail하면 이미 DB에 들어가있는 상태라 구분이 안됨
            String redirectUrl;
            if ((boolean) kakaoResponse.get("isNewAccount")) {
                redirectUrl = "/register";
            } else {
                redirectUrl = "/home";
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("access_token", kakaoResponse.get("accessToken"));
            responseBody.put("redirect_url", redirectUrl);
            responseBody.put("name", kakaoResponse.get("name"));
            responseBody.put("email", kakaoResponse.get("email"));
            responseBody.put("user_pk", kakaoResponse.get("userPk"));

            String jsonResponse = gson.toJson(responseBody);
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        } catch (Exception e) {
            // 로그인 실패
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "fail");
            errorResponse.put("message", "로그인 실패했어요");
            errorResponse.put("error", e.getMessage());

            String errorJsonResponse = gson.toJson(errorResponse);
            return new ResponseEntity<>(errorJsonResponse, HttpStatus.BAD_REQUEST); // 400

        }
    }



}