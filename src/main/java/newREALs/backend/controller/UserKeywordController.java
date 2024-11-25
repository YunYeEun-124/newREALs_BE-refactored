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
        if (tempToken == null || tempToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.failure("E500","Authorization 헤더가 비어 있습니다."));
        }

        // 임시 토큰 검증
        Claims claims;
        try {
            claims = tokenService.validateAndParseToken(tempToken, "temporary");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.failure("E500","JWT 토큰이 유효하지 않습니다."));
        }

        String email = claims.getSubject();
        String name = claims.get("name", String.class);
        String profilePath = claims.get("profilePath", String.class);

        // 키워드 유효성 검증
        if (keywords.isEmpty() || keywords.size() > 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.failure("E500","키워드는 1개 이상, 최대 6개까지 입력 가능합니다."));
        }

        for (String key : keywords) {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.failure("E500","키워드 리스트에 빈 값이 포함되어 있습니다."));
            }
        }

        // Accounts 생성 및 데이터베이스 저장
        Accounts user = Accounts.builder()
                .email(email)
                .name(name)
                .profilePath(profilePath)
                .build();
        try {
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.failure("E500","유저 정보를 저장하는 중 문제가 발생했습니다."));
        }

        Long userId = user.getId();

        // 키워드 저장
        List<String> createdUserKeywords = userKeywordService.createUserKeywords(keywords, userId);
        if (createdUserKeywords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.failure("E500","유저 관심 키워드를 저장하는 중 문제가 발생했습니다."));
        }

        // 토큰 생성
        Map<String, Object> responseBody = new HashMap<>();
        try {
            responseBody.put("access_token", tokenService.generateAccessToken(user));
            responseBody.put("refresh_token", tokenService.generateRefreshToken(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.failure("E500","토큰 생성에 실패했습니다."));
        }

        responseBody.put("keywords", createdUserKeywords);
        responseBody.put("name", user.getName());
        responseBody.put("email", user.getEmail());
        responseBody.put("profilePath", user.getProfilePath());

        return ResponseEntity.ok(
                ApiResponseDTO.success("유저 키워드 저장 및 회원가입 완료", responseBody)
        );
    }


}