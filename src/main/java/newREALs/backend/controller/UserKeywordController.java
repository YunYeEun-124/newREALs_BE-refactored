package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserKeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class UserKeywordController {

    private final UserKeywordService userKeywordService;
    private final TokenService tokenService;


    @PutMapping("/edit")
    public ResponseEntity<?> editUserKeywords(HttpServletRequest userInfo, @RequestBody List<String> keywords){
        Long userid = tokenService.getUserId(userInfo);
        List<String> updateUserKeywods = userKeywordService.updateUserKeywords(keywords,userid);

        //출력
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String,List<String>> keys = new LinkedHashMap<>();

        keys.put("keywords",updateUserKeywods);

        response.put("isSuccess", true);
        response.put("code", "S200");
        response.put("message", "키워드 등록 성공");
        response.put("data", keys);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //처음 키워드 만들기.
    @PostMapping
    public ResponseEntity<?> registerUserKeywords(HttpServletRequest userInfo,@RequestBody List<String> keywords){
        Long userid = tokenService.getUserId(userInfo);
        Map<String, Object> response = new LinkedHashMap<>();

        //keywords is null case
        if(keywords.isEmpty() || keywords.size() > 6) {
            response.put("isSuccess", false);
            response.put("code", "E400");
            response.put("message", "keywords are wrong ");
            response.put("data",null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //key
        //실제 도메인 객체 생성
        List<UserKeyword> createdUserKeywords = userKeywordService.createUserKeywords(keywords,userid);

        if(createdUserKeywords.isEmpty()) {
            response.put("isSuccess", false);
            response.put("code", "E400");
            response.put("message", "이미 유저의 관심 키워드가 존재합니다.");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }



        List<String> result = new ArrayList<>();
        for(UserKeyword userKeyword : createdUserKeywords)
            result.add(userKeyword.getKeyword().getName());

        Map<String,List<String>> keys = new LinkedHashMap<>();
        keys.put("keywords",result);

        response.put("isSuccess", true);
        response.put("code", "S200");
        response.put("message", "키워드 등록 성공");
        response.put("data", keys);


        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}