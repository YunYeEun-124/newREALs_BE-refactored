package newREALs.backend.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.dto.DailyNewsThumbnailDTO;
import newREALs.backend.dto.KeywordNewsDTO;
import newREALs.backend.service.NewsService;
import newREALs.backend.service.NewsService2;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainNewsController {

    private final NewsService2 newsService;
    private final TokenService tokenService;

    //main news list
    @GetMapping("/daily")
    public ResponseEntity<?> viewDailynewsList(HttpServletRequest userInfo){

        List<DailyNewsThumbnailDTO> result = newsService.getDailynewsList();
        if(result.size() != 5){ //error
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", 400);
            errorResponse.put("error", "Dailynews List is not ready");
            errorResponse.put("path", "/main/daily");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        }else  return  ResponseEntity.ok().body(result);



    }

    //keyword newsList, 사용자 키워드 리스트
    @GetMapping("/keyword")
    public ResponseEntity<?> viewKeywordnewsList(HttpServletRequest userInfo, @RequestParam int keywordIndex,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);
        KeywordNewsDTO keywordnewsList =  newsService.getKeywordnewsList(userid,keywordIndex,page);

        return ResponseEntity.ok().body(keywordnewsList);
    }

}