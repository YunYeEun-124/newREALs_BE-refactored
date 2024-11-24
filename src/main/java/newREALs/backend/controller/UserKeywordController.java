package newREALs.backend.controller;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserKeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/register")
public class UserKeywordController {

    private final UserKeywordService userKeywordService;
    private final TokenService tokenService;
    private final UserRepository userRepository;


    @PutMapping("/edit")
    public ResponseEntity<?> editUserKeywords(HttpServletRequest userInfo, @RequestBody List<String> keywords){
        Long userid = tokenService.getUserId(userInfo);

        //keywords is null case
        if(keywords.isEmpty() || keywords.size() > 6) {
            throw new IllegalArgumentException("매개변수 사이즈 오류");
        }
        for(String key : keywords){
            if(key.isEmpty())  throw new IllegalArgumentException("매개변수 is null ");

        }


        List<String> updateUserKeywods = userKeywordService.updateUserKeywords(keywords,userid);

        //출력
        if(updateUserKeywods.isEmpty())  throw new IllegalStateException("유저 관심 키워드 변경 실패");

        Map<String,List<String>> keys = new LinkedHashMap<>();
        keys.put("keywords",updateUserKeywods);

        return ResponseEntity.ok(
                ApiResponseDTO.success( "유저 관심 키워드 변경 성공", keys)
        );
    }
    @PostMapping
    public ResponseEntity<?> registerUserKeywords(
            HttpServletRequest request,
            @RequestBody List<String> keywords) {

        // 헤더에서 토큰 추출
        String tempToken = tokenService.extractTokenFromHeader(request);
        if (tempToken == null) {
            throw new IllegalArgumentException("Authorization 헤더가 비어 있습니다.");
        }

        // 임시 토큰 검증
        Claims claims = tokenService.validateAndParseToken(tempToken, "temporary");
        String email = claims.getSubject(); // 토큰에서 이메일 추출
        String name = claims.get("name", String.class);
        String profilePath = claims.get("profilePath", String.class);



        // 키워드 유효성 검증
        if (keywords.isEmpty() || keywords.size() > 6) {
            throw new IllegalArgumentException("매개변수 사이즈 오류");
        }
        for (String key : keywords) {
            if (key.isEmpty()) throw new IllegalArgumentException("매개변수에 비어있는 키워드가 포함되어 있습니다.");
        }
        // Accounts 생성 및 데이터베이스에 저장
        Accounts user = Accounts.builder()
                .email(email)
                .name(name)
                .profilePath(profilePath)
                .build();
        userRepository.saveAndFlush(user);  //바로 데이터베이스에 저장되도록

        Long userId=user.getId();
        // 키워드 저장
        List<String> createdUserKeywords = userKeywordService.createUserKeywords(keywords, userId);
        if (createdUserKeywords.isEmpty()) {
            throw new IllegalStateException("유저 관심 키워드 저장 실패");
        }

        // 유저 상태 업데이트 및 최종 토큰 발급
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        // 응답 객체 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", accessToken);
        responseBody.put("refresh_token", refreshToken);
        responseBody.put("keywords", createdUserKeywords);
        responseBody.put("name",user.getName());
        responseBody.put("email",user.getEmail());
        responseBody.put("profilePath",user.getProfilePath());

        return ResponseEntity.ok(
                ApiResponseDTO.success("유저 키워드 저장 및 회원가입 완료", responseBody)
        );
    }

}