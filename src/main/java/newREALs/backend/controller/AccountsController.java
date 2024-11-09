package newREALs.backend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.service.KakaoService;
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
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // 이렇게 해야 줄바꿈됨

    @PostMapping("/login")
    public ResponseEntity<String> kakaoLogin(@RequestBody Map<String, String> request, HttpServletResponse response) {
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
