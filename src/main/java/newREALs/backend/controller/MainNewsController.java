package newREALs.backend.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.ThinkComment;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.dto.DailyNewsThumbnailDTO;
import newREALs.backend.dto.InsightDTO;
import newREALs.backend.dto.KeywordNewsDTO;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.service.InsightService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainNewsController {

    private final NewsService2 newsService;
    private final TokenService tokenService;
    private final UserKeywordRepository userKeywordRepository;
    private final InsightService insightService;

    //main news list
    @GetMapping("/daily")
    public ResponseEntity<?> viewDailynewsList(HttpServletRequest userInfo){

        List<DailyNewsThumbnailDTO> list = newsService.getDailynewsList();
        HashMap<String,List<DailyNewsThumbnailDTO>> result= new HashMap<>();
        result.put("dailynewsList",list);
        if(list.size() != 5){ //error
            throw new IllegalStateException("dailynews 5개 조회 실패. 서버 문제");
        }else  return ResponseEntity.ok(
                ApiResponseDTO.success( "main/daily 뉴스 5개 조회 성공", result)
        );



    }

    //keyword newsList, 사용자 키워드 리스트
    @GetMapping("/keyword")
    public ResponseEntity<?> viewKeywordnewsList(HttpServletRequest userInfo, @RequestParam int keywordIndex,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);

        if(userKeywordRepository.findAllByUserId(userid).size() <= keywordIndex || keywordIndex < 0 ){
            throw  new IllegalArgumentException("keywordIndex 범위 오류");
        }
        KeywordNewsDTO keywordnewsList =  newsService.getKeywordnewsList(userid,keywordIndex,page);
        if (page <= 0 ||page > keywordnewsList.getTotalPage())
            throw  new IllegalArgumentException("페이지 범위 오류");



        return ResponseEntity.ok(
                ApiResponseDTO.success( "main/keyword 뉴스 리스트 조회 성공 ", keywordnewsList)
        );
    }

    @GetMapping("/insight")
    public ResponseEntity<?> viewInsightList(){

       List<InsightDTO> list = insightService.getInsight();

        if(list.isEmpty()){
            throw new IllegalStateException("insight 리스트 조회 실패. 서버 문제");
        }

        HashMap<String,List<InsightDTO>> result = new HashMap<>();
        result.put("insightList",list);

        return ResponseEntity.ok(
                ApiResponseDTO.success( "main/insight 리스트 조회 성공 ", result)
        );
    }

}