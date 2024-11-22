package newREALs.backend.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.*;
import newREALs.backend.repository.AccountsRepository;
import newREALs.backend.service.ProfileService;
import newREALs.backend.service.QuizService;
import newREALs.backend.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final AccountsRepository accountsRepository;

    //[get] 프로필 페이지 - 퀴즈 정보
    @GetMapping("/quiz")
    public ResponseEntity<ApiResponseDTO<List<QuizStatusDto>>> getQuizStatus(HttpServletRequest request) {
        //토큰이 유효하지 않으면 getUserId에서 401에러 "토큰이 유효하지 않습니다"를 반환함
        Long userId = tokenService.getUserId(request);
        List<QuizStatusDto> quizStatusList = quizService.getQuizStatus(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("퀴즈 상태 조회 성공", quizStatusList));

    }

    //[get] 프로필페이지 - 유저 정보
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<ProfileInfoDto>> getProfileInfo(HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        ProfileInfoDto profileInfoDTO = profileService.getProfileInfo(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("유저 정보 조회 성공", profileInfoDTO));
    }


    //[get] 프로필페이지 - 출석 현황
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceList(HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        ProfileAttendanceListDto profileAttendanceListDTO = profileService.getAttendanceList(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("유저 출석 현황 조회 성공", profileAttendanceListDTO));
    }

    //[get] 프로필 페이지 - 유저 스크랩 목록
    @GetMapping("/scrap")
    public ResponseEntity<?> getScrapList(HttpServletRequest request, @RequestParam(required = false) Integer page) {
        if (page == null) {
            throw new IllegalArgumentException("파라미터가 비어있습니다.");
        }

        Long userId = tokenService.getUserId(request);
        Page<BaseNewsThumbnailDTO> scrapNewsPage = profileService.getScrapNewsThumbnail(userId, page);

        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("basenewsList", scrapNewsPage.getContent());
        response.put("totalPage", scrapNewsPage.getTotalPages());
        response.put("totalElement", scrapNewsPage.getTotalElements());

        return ResponseEntity.ok(ApiResponseDTO.success("유저 스크랩 목록 조회 성공", response));
    }

    //[get] 프로필 페이지 - 유저 관심사
    @GetMapping("/interest")
    public ResponseEntity<?> getInterest(HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        Map<String, List<ProfileInterestDto>> interestMap = profileService.getInterest(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("interest", interestMap);
        return ResponseEntity.ok(ApiResponseDTO.success("유저 관심도 조회 성공", response));
    }

    // profile patch
    @PatchMapping("/edit")
    public ResponseEntity<ApiResponseDTO<?>> ProfileEdit(HttpServletRequest request,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) MultipartFile file) throws IOException {
        Long userId = tokenService.getUserId(request);

        if ((name == null || name.isBlank()) && (file == null || (file.getSize() == 0))) {
            throw new IllegalArgumentException("변경된 정보가 없습니다.");
        }

        profileService.editProfile(userId, name, file);
        return ResponseEntity.ok(ApiResponseDTO.success("프로필 변경 성공", null));
    }

    @DeleteMapping("/unscrap")
    public ResponseEntity<?> ProfileUnscrap(HttpServletRequest request, @RequestParam(required = false) Long newsId) {
        if(newsId == null) {
            throw new IllegalArgumentException("파라미터가 비어있습니다.");
        }

        Long userId = tokenService.getUserId(request);
        boolean exists = profileService.isScrapped(userId, newsId);
        if (!exists) {
            throw new EntityNotFoundException("스크랩 목록에 해당 id의 뉴스가 없습니다.");
        }

        profileService.deleteScrap(userId, newsId);
        return ResponseEntity.ok(ApiResponseDTO.success("스크랩 해제 성공", null));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getScrapSearchList(HttpServletRequest request, @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer page) {
        if (keyword == null || page == null) {
            throw new IllegalArgumentException("파라미터가 비어있습니다.");
        }
        Long userId = tokenService.getUserId(request);
        Page<BaseNewsThumbnailDTO> scrapSearchPage = profileService.getScrapSearchList(userId, keyword, page);

        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("searchList", scrapSearchPage.getContent());
        response.put("totalPage", scrapSearchPage.getTotalPages());
        response.put("totalElement", scrapSearchPage.getTotalElements());

        return ResponseEntity.ok(ApiResponseDTO.success("스크랩 목록 검색 조회 성공", response));
    }
}
