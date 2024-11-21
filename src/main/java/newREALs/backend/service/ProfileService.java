package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.dto.*;
import newREALs.backend.domain.*;
import newREALs.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final AccountsRepository accountsRepository;
    private final ScrapRepository scrapRepository;
    private final BaseNewsRepository baseNewsRepository;


    //[get] 프로필 정보
    public ProfileInfoDto getProfileInfo(Long userId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 유저 키워드 리스트에 저장하기
        List<Keyword> userKeywords = userKeywordRepository.findKeywordsById(userId);
        List<String> keywordList = new ArrayList<>();
        for(Keyword userKeyword : userKeywords){
            keywordList.add(userKeyword.getName());
        }

        return ProfileInfoDto.builder()
                .user_id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePath(user.getProfilePath())
                .point(user.getPoint())
                .keywords(keywordList)
                .build();
    }


    //[get] 출석현황
    public ProfileAttendanceListDto getAttendanceList(Long userId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        List<Boolean> attendanceList = accountsRepository.findAttendanceListById(userId);

        return ProfileAttendanceListDto.builder()
                .user_id(user.getId())
                .attendanceList(attendanceList)
                .build();
    }


    public Pageable getPageInfo(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("basenews.uploadDate"));
        return PageRequest.of(page - 1, 6, Sort.by(sorts));
    }

    //[get] 스크랩 보여주기
    public Page<BaseNewsThumbnailDTO> getScrapNewsThumbnail(Long userId, int page) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Pageable pageable = getPageInfo(page);

        // 스크랩 된 뉴스 가져와
        //Page<Basenews> scrapNewsPage = accountsRepository.findScrapNewsById(userId, pageable);
        Page<Scrap> scrapNewsList = scrapRepository.findByUser(user, pageable);

        return scrapNewsList.map(scrap -> {
            Basenews basenews = scrap.getBasenews();
            return BaseNewsThumbnailDTO.builder()
                    .basenewsId(basenews.getId())
                    .category(basenews.getCategory().getName())
                    .subCategory(basenews.getSubCategory().getName())
                    .keyword(basenews.getKeyword().getName())
                    .title(basenews.getTitle())
                    .summary(basenews.getSummary())
                    .imageUrl(basenews.getImageUrl())
                    .date(basenews.getUploadDate()) // Basenews의 uploadDate 필드
                    .isScrap(true)
                    .build();
        });
    }


    //관심도 분석
    public Map<String, List<ProfileInterestDto>> getInterest(Long userId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Map<String, List<ProfileInterestDto>> result = new HashMap<>();

        Pageable three = PageRequest.of(0,3);

        result.put("total", new ArrayList<>());
        result.put("politics", new ArrayList<>());
        result.put("economy", new ArrayList<>());
        result.put("society", new ArrayList<>());

        // 카테고리 상관 없이 전체에서 3개 가져오기
        List<Object[]> totalInterest = accountsRepository.findTotalInterestById(userId, three);
        List<ProfileInterestDto> totalInterestDTO = getPercentage(totalInterest);
        result.put("total", totalInterestDTO); // key를 total로

        // 카테고리 별로 3개 가져오기
        List<Object[]> societyInterest = accountsRepository.findCategoryInterestById(userId, "society", three);
        List<Object[]> politicsInterest = accountsRepository.findCategoryInterestById(userId, "politics", three);
        List<Object[]> economyInterest = accountsRepository.findCategoryInterestById(userId, "economy", three);


        List<ProfileInterestDto> societyInterestDTO = getPercentage(societyInterest);
        List<ProfileInterestDto> politicsInterestDTO = getPercentage(politicsInterest);
        List<ProfileInterestDto> economyInterestDTO = getPercentage(economyInterest);

        result.put("society", societyInterestDTO);
        result.put("politics", politicsInterestDTO);
        result.put("economy", economyInterestDTO);

        return result;
    }

    //[get] 관심도 분석 비율 찾기
    private List<ProfileInterestDto> getPercentage(List<Object[]> interests) {
        List<ProfileInterestDto> interestDTOList = new ArrayList<>();

        int total = 0;
        for (Object[] item : interests) {
            total += (int) item[2];
        }

        int percentageSum = 0;

        for (Object[] item : interests) {
            String category = (String) item[0];
            String subCategory = (String) item[1];
            int count = (int) item[2];

            int percentage = (int) Math.round((count * 100.0) / total);
            ProfileInterestDto dto = ProfileInterestDto.builder()
                    .category(category)
                    .subCategory(subCategory)
                    .percentage(percentage)
                    .build();
            interestDTOList.add(dto);

            percentageSum += percentage;

        }
        int difference = 100 - percentageSum;

        // 퍼센트 합 100 안되면 제일 큰 항목에 그 차이만큼 더해주기
        if (difference != 0 && !interestDTOList.isEmpty()) {
            ProfileInterestDto largest = interestDTOList.get(0);
            largest.setPercentage(largest.getPercentage() + difference);
        }
        return interestDTOList;
    }

    // 지금은 저장위치를 로컬로 해놓음..
    @Value("${file.upload-dir}")
    private String uploadDir;
    public void editProfile(Long userId, String newName, MultipartFile file) throws IOException {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 이름 변경
        if (newName != null && !newName.isEmpty()) {
            user.setName(newName);
        }

        if (file != null && !file.isEmpty()) {
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("디렉토리를 생성할 수 없습니다: " + uploadDir);
                }
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File saveFile = new File(uploadDir + fileName);
            file.transferTo(saveFile);

            // 일단 로컬로 설정
            String newProfilePath = "http://localhost:8080/" + uploadDir + fileName;
            user.setProfilePath(newProfilePath);
        }
        userRepository.save(user);
    }

    public void deleteScrap(Long userId, Long newsId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));
        Basenews news = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("없는 뉴스 ID"));

        Scrap scrap = scrapRepository.findByUserAndBasenews(user, news)
                .orElseThrow(() -> new IllegalArgumentException("스크랩된 뉴스가 아닙니다."));

        // scrap 삭제
        scrapRepository.delete(scrap);
    }

    public Page<BaseNewsThumbnailDTO> getScrapSearchList(Long userId, String keyword, int page){
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Pageable pageable = getPageInfo(page);

        Page<Basenews> scrapSearchList = scrapRepository.findByUserAndTitleContainingOrDescriptionContaining(userId, keyword, pageable);

        return scrapSearchList.map(basenews -> BaseNewsThumbnailDTO.builder()
                .basenewsId(basenews.getId())
                .category(basenews.getCategory().getName())
                .subCategory(basenews.getSubCategory().getName())
                .keyword(basenews.getKeyword().getName())
                .title(basenews.getTitle())
                .summary(basenews.getSummary())
                .imageUrl(basenews.getImageUrl())
                .date(basenews.getUploadDate())
                .isScrap(true)
                .build());
    }
}