package newREALs.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProfilePageDTO {
    // 관심도 분석 보여주기
    // 스크랩 뉴스 리스트 보여주기 -> BaseNewsThumbnailDTO
    // 오늘 퀴즈 현황 (1~5) -> QuizStatusDTO
    // 프로필 정보 -> ProfileInfoDTO
    // 출석 달력 -> AttendanceCalenderDTO

    private List<BaseNewsThumbnailDTO> baseNewslist;
    private List<ProfileInfoDTO> profileinfo;


}
