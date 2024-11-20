package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.*;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.QuizService;
import newREALs.backend.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts/profile")
public class ProfileController {
    private final QuizService quizService;
    private final TokenService tokenService;
    private final ProfileService profileService;

    //[get] 프로필 페이지 나의 퀴즈 현황
    @GetMapping("/quiz")
    public ResponseEntity<List<QuizStatusDto>> getQuizStatus(HttpServletRequest userInfo) {
        Long userId = tokenService.getUserId(userInfo);

        List<QuizStatusDto> quizStatusList = quizService.getQuizStatus(userId);
        return ResponseEntity.ok(quizStatusList);
    }

    //[get] 프로필페이지 - 유저 정보
    @GetMapping("/info")
    public ResponseEntity<?> getProfileInfo(HttpServletRequest request) {
        try {
            String token = tokenService.extractTokenFromHeader(request);

            if (token == null || !tokenService.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
            Long userId = tokenService.extractUserIdFromToken(token);

            ProfileInfoDto profileInfoDTO = profileService.getProfileInfo(userId);
            return ResponseEntity.ok(profileInfoDTO);

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 토큰 -> 401
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // 다른 에러들 -> 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    //[get] 프로필페이지 - 출석 현황
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceList(HttpServletRequest request) {
        try {
            String token = tokenService.extractTokenFromHeader(request);

            if (token == null || !tokenService.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
            Long userId = tokenService.extractUserIdFromToken(token);

            ProfileAttendanceListDto profileAttendanceListDTO = profileService.getAttendanceList(userId);
            return ResponseEntity.ok(profileAttendanceListDTO);

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 토큰 -> 401
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // 다른 에러들 -> 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    //[get] 프로필 페이지 - 유저 스크랩 목록
    @GetMapping("/scrap")
    public ResponseEntity<?> getScrapList(HttpServletRequest request, @RequestParam int page) {
        try {
            String token = tokenService.extractTokenFromHeader(request);

            if (token == null || !tokenService.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
            Long userId = tokenService.extractUserIdFromToken(token);

            Page<BaseNewsThumbnailDTO> scrapNewsPage = profileService.getScrapNewsThumbnail(userId, page);

            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("basenewsList", scrapNewsPage.getContent());
            response.put("totalPage", scrapNewsPage.getTotalPages());
            response.put("totalElement", scrapNewsPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 토큰 -> 401
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // 다른 에러들 -> 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping("/interest")
    public ResponseEntity<?> getInterest(HttpServletRequest request) {
        try {
            String token = tokenService.extractTokenFromHeader(request);

            if (token == null || !tokenService.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 토큰이에요");
            }
            Long userId = tokenService.extractUserIdFromToken(token);

            Map<String, List<ProfileInterestDto>> interestMap = profileService.getInterest(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("interest", interestMap);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 토큰 -> 401
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // 다른 에러들 -> 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "실패했어요");
            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
            errorResponse.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    //
//    @PatchMapping("profile/edit")
//    public ResponseEntity<?> ProfileEdit(HttpServletRequest request, @RequestParam MultipartFile file) {
//        try {
//            String token = tokenService.extractTokenFromHeader(request);
//
//            if (token == null || !tokenService.validateToken(token)) {
//                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
//            }
//            Long userId = tokenService.extractUserIdFromToken(token);
//
//            String updatedProfileUrl = profileService.editProfile(userId, profileEditDTO);
//
//
//        } catch (IllegalArgumentException e) {
//            // 유효하지 않은 토큰 -> 401
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "실패했어요");
//            errorResponse.put("error", "401 Unauthorized: " + e.getMessage());
//            errorResponse.put("status", "fail");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//
//        } catch (Exception e) {
//            // 다른 에러들 -> 400
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "실패했어요");
//            errorResponse.put("error", "400 Bad Request: \"" + e.getMessage() + "\"");
//            errorResponse.put("status", "fail");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//    }

}
