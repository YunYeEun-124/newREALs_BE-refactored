package newREALs.backend.controller;

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
        if (tempToken == null || !tokenService.validateToken(tempToken) || !"temporary".equals(tokenService.getTokenType(tempToken))) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        // 유저 ID 추출 및 처리
        Long userId = tokenService.extractUserIdFromToken(tempToken);
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 토큰과 일치하는 유저가 없습니다"));

        // 키워드 유효성 검증
        if (keywords.isEmpty() || keywords.size() > 6) {
            throw new IllegalArgumentException("매개변수 사이즈 오류");
        }
        for (String key : keywords) {
            if (key.isEmpty()) throw new IllegalArgumentException("매개변수에 비어있는 키워드가 포함되어 있습니다.");
        }

        // 키워드 저장
        List<String> createdUserKeywords = userKeywordService.createUserKeywords(keywords, userId);
        if (createdUserKeywords.isEmpty()) {
            throw new IllegalStateException("유저 관심 키워드 저장 실패");
        }

        // 유저 상태 업데이트 및 최종 토큰 발급
        userKeywordService.completeUserProfile(userId); // 유저 추가정보 등록 완료 처리
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        // 응답 객체 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", accessToken);
        responseBody.put("refresh_token", refreshToken);
        responseBody.put("keywords", createdUserKeywords);

        return ResponseEntity.ok(
                ApiResponseDTO.success("유저 관심 키워드 저장 성공", responseBody)
        );
    }

}