package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserKeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class UserKeywordController {

    private final UserKeywordService userKeywordService;
    private final TokenService tokenService;


    @PutMapping("/edit")
    public ResponseEntity<List<String>> editUserKeywords(HttpServletRequest userInfo, @RequestBody List<String> keywords){
        Long userid = tokenService.getUserId(userInfo);
        List<String> updateUserKeywods = userKeywordService.updateUserKeywords(keywords,userid);


        return ResponseEntity.status(HttpStatus.CREATED).body(updateUserKeywods);
    }

    //처음 키워드 만들기.
    @PostMapping
    public ResponseEntity<List<String>> registerUserKeywords(HttpServletRequest userInfo,@RequestBody List<String> keywords){
        Long userid = tokenService.getUserId(userInfo);
        //실제 도메인 객체 생성
        List<UserKeyword> createdUserKeywords = userKeywordService.createUserKeywords(keywords,userid);
        //just 반환값
        List<String> result = new ArrayList<>();

        for(UserKeyword userKeyword : createdUserKeywords)
            result.add(userKeyword.getKeyword().getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);

    }
}