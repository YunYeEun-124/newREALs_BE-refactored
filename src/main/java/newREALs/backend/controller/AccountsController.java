package newREALs.backend.controller;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import newREALs.backend.service.KakaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final KakaoService kakaoService;
    private final Gson gson = new Gson();

    @PostMapping("/login")
    public ResponseEntity<String> kakaoLogin(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("code");
        Map<String, Object> kakaoResponse = kakaoService.processKakaoLogin(authorizationCode);

        String redirectUrl;
        if ((boolean) kakaoResponse.get("isNewAccount")) {
            redirectUrl = "/register";
        } else {
            redirectUrl = "/home";
        }

        // 프론트에 전달할 내용
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("access_token", kakaoResponse.get("accessToken"));
        responseBody.put("redirect_url", redirectUrl);
        responseBody.put("name", kakaoResponse.get("name"));
        responseBody.put("email", kakaoResponse.get("email"));
        responseBody.put("user_pk", kakaoResponse.get("userPk"));

        // JSON 응답으로
        String jsonResponse = gson.toJson(responseBody);

        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }
}
