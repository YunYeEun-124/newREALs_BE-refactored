package newREALs.backend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.DTO.ProfileAttendanceListDTO;
import newREALs.backend.DTO.ProfileInfoDTO;
import newREALs.backend.DTO.ProfileQuizStatusDTO;
import newREALs.backend.service.KakaoService;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final ProfileService profileService;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // 이렇게 해야 줄바꿈됨

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

//    @GetMapping("/profile/info")
//    public ResponseEntity<?> getProfileInfo(HttpServletRequest request) {
//        String token = tokenService.extractTokenFromHeader(request);
//
//        // 유효하지 않은 토큰인 경우 -> 401
//        if (token == null || !tokenService.validateToken(token)) {
//            throw new IllegalArgumentException("유효하지 않은 토큰이에요");
//        }
//
//        Long userId = tokenService.extractUserIdFromToken(token);
//
//        ProfileInfoDTO profileInfoDTO = profileService.getProfileInfo(userId);
//        return ResponseEntity.ok(profileInfoDTO);
//    }
@GetMapping("/profile/info")
public ResponseEntity<?> getProfileInfo(HttpServletRequest request) {
    try {
        String token = tokenService.extractTokenFromHeader(request);

        if (token == null || !tokenService.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        Long userId = tokenService.extractUserIdFromToken(token);

        ProfileInfoDTO profileInfoDTO = profileService.getProfileInfo(userId);
        return ResponseEntity.ok(profileInfoDTO);

    } catch (IllegalArgumentException e) {
        // 유효하지 않은 토큰 -> 401
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "유효하지 않은 토큰이에요");
        errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
        errorResponse.put("status", "fail");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

    } catch (Exception e) {
        // 다른 에러들 -> 400
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "실패했어요");
        errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
        errorResponse.put("status", "fail");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}

    @GetMapping("/profile/quiz")
    public ResponseEntity<?> getProfileQuizStatus(HttpServletRequest request) {
        try {
            String token = tokenService.extractTokenFromHeader(request);

            if (token == null || !tokenService.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
            Long userId = tokenService.extractUserIdFromToken(token);

            ProfileQuizStatusDTO profileQuizStatusDTO = profileService.getQuizStatus(userId);
            return ResponseEntity.ok(profileQuizStatusDTO);

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 토큰 -> 401
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "유효하지 않은 토큰이에요");
            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // 다른 에러들 -> 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }


    public ResponseEntity<ProfileAttendanceListDTO> getAttendanceList(HttpServletRequest request) {
        String token = tokenService.extractTokenFromHeader(request);

        // 유효하지 않은 토큰인 경우 -> 401
        if (token == null || !tokenService.validateToken(token)) {
            return ResponseEntity.status(401).body(null);
        }
        Long userId = tokenService.extractUserIdFromToken(token);

        ProfileAttendanceListDTO profileAttendanceListDTO = profileService.getAttendanceList(userId);
        return ResponseEntity.ok(profileAttendanceListDTO);
    }

}
