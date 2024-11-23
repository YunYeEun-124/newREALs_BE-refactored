package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserKeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class UserKeywordController {

    private final UserKeywordService userKeywordService;
    private final TokenService tokenService;


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

    //처음 키워드 만들기.
    @PostMapping
    public ResponseEntity<?> registerUserKeywords(HttpServletRequest userInfo,@RequestBody List<String> keywords)  {
        Long userid = tokenService.getUserId(userInfo);
        Map<String, Object> response = new LinkedHashMap<>();

        //keywords is null case
        if(keywords.isEmpty() || keywords.size() > 6) {
            throw new IllegalArgumentException("매개변수 사이즈 오류");
        }
        for(String key : keywords){
            if(key.isEmpty())  throw new IllegalArgumentException("매개변수 is null ");
        }

        List<String> createdUserKeywords = userKeywordService.createUserKeywords(keywords,userid);
        Map<String,List<String>> keys = new LinkedHashMap<>();

        keys.put("keywords",createdUserKeywords);

        if(createdUserKeywords.isEmpty())
          throw new IllegalStateException("유저 관심 키워드 저장 실패");

        return ResponseEntity.ok(
                ApiResponseDTO.success( "유저 관심 키워드 저장 성공", keys)
        );

    }
}