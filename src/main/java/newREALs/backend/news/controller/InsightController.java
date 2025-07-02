package newREALs.backend.news.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.accounts.dto.ProfileInterestProjection;
import newREALs.backend.common.dto.ApiResponseDTO;
import newREALs.backend.news.dto.RequestUserCommentDTO;
import newREALs.backend.news.service.InsightService;
import newREALs.backend.common.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/insight")
public class InsightController {
    private final TokenService tokenService;
    private final InsightService insightService;
    //해당 뉴스의 인사이트가 없으면 200-null
    //해동 뉴스의 유저코멘트가 없으면 200-topic
    //해당 뉴스의 유저코멘트가 잇어면 200- topic, usercomment,aicomment
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInsight(@PathVariable Long id, HttpServletRequest request){
        Long userId = tokenService.getUserId(request);
        ProfileInterestProjection.ResponseUserCommentDTO result = insightService.getUserInsight(userId,id);

        if(result == null){
            return ResponseEntity.ok(
                    ApiResponseDTO.success("해당 뉴스에는 인사이트 기능이 없습니다.",null));
        }

        return ResponseEntity.ok(
                ApiResponseDTO.success("유저 인사이트 조회 성공!",result));
    }


    //
    @PostMapping("/{id}")
    public ResponseEntity<?> saveUserInsight(@PathVariable Long id, //newsid
                                        @RequestBody(required = false) RequestUserCommentDTO userComment,
                                        HttpServletRequest request){
        Long userId = tokenService.getUserId(request);

        if(userComment.getComment().isEmpty() || userComment == null)
            throw new IllegalArgumentException("user의 Comment가 비어있습니다.");

        String message = insightService.saveUserInsight(userComment.getComment(), userId, id);
        if (message == null) {
            throw  new IllegalStateException("user comment 저장 실패",null);
        }

        return ResponseEntity.ok(
                ApiResponseDTO.success(message,null));
    }

}
