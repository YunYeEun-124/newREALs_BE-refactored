package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.dto.*;
import newREALs.backend.domain.*;
import newREALs.backend.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final SubInterestRepository subInterestRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final S3Service s3Service;
    private final PreSubInterestRepository preSubInterestRepository;


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
                    .isScrapped(true)
                    .build();
        });
    }


    //관심도 분석
    public Map<String, List<ProfileInterestDto>> getInterest(Long userId) {
        Map<String, List<ProfileInterestDto>> result = new HashMap<>();

        result.put("전체", new ArrayList<>());
        result.put("정치", new ArrayList<>());
        result.put("경제", new ArrayList<>());
        result.put("사회", new ArrayList<>());

        // 카테고리 상관 없이 전체에서 3개 가져오기
        List<ProfileInterestProjection> totalInterest = accountsRepository.findTotalInterestById(userId);
        List<ProfileInterestDto> totalInterestDTO = getPercentage(totalInterest);
        result.put("전체", totalInterestDTO); // key를 total로

        // 카테고리 별로 3개 가져오기
        List<ProfileInterestProjection> societyInterest = accountsRepository.findCategoryInterestById(userId, "사회");
        List<ProfileInterestProjection> politicsInterest = accountsRepository.findCategoryInterestById(userId, "정치");
        List<ProfileInterestProjection> economyInterest = accountsRepository.findCategoryInterestById(userId, "경제");

        List<ProfileInterestDto> societyInterestDTO = getPercentage(societyInterest);
        List<ProfileInterestDto> politicsInterestDTO = getPercentage(politicsInterest);
        List<ProfileInterestDto> economyInterestDTO = getPercentage(economyInterest);

        result.put("사회", societyInterestDTO);
        result.put("정치", politicsInterestDTO);
        result.put("경제", economyInterestDTO);

        return result;
    }



    //[get] 관심도 분석 비율 찾기
    private List<ProfileInterestDto> getPercentage(List<ProfileInterestProjection> interests) {
        List<ProfileInterestDto> interestDTOList = new ArrayList<>();

        int total = 0;
        for (ProfileInterestProjection item : interests) {
            total += item.getCount();
        }
        if(total == 0) {
            return interestDTOList;
        }

        int percentageSum = 0;

        for (ProfileInterestProjection item : interests) {
            String category = item.getCategory();
            String subCategory = item.getSubCategory();
            int count = item.getCount();

            int percentage = (int) Math.round((count * 100.0) / total);
            if (percentage > 0) {
                ProfileInterestDto dto = ProfileInterestDto.builder()
                        .category(category)
                        .subCategory(subCategory)
                        .percentage(percentage)
                        .build();
                interestDTOList.add(dto);

                percentageSum += percentage;
            }

        }
        int difference = 100 - percentageSum;

        // 퍼센트 합 100 안되면 제일 큰 항목에 그 차이만큼 더해주기
        if (difference != 0 && !interestDTOList.isEmpty()) {
            ProfileInterestDto largest = interestDTOList.get(0);
            largest.setPercentage(largest.getPercentage() + difference);
        }
        return interestDTOList;
    }

    public void editProfile(Long userId, String newName, MultipartFile file) throws IOException {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 이름 변경

        if (newName != null && !newName.isEmpty()) {
            user.setName(newName);
        }

        if(file!=null&&!file.isEmpty()) {
            String s3Url = s3Service.uploadImageFile(file);
            user.setProfilePath(s3Url);
        }

//        if (file != null && !file.isEmpty()) {
//            File directory = new File(uploadDir);
//
//            if (!directory.exists()) {
//                if (!directory.mkdirs()) {
//                    throw new IOException("디렉토리를 생성할 수 없습니다: " + uploadDir);
//                }
//            }

//            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//            File saveFile = new File(uploadDir + fileName);
//            file.transferTo(saveFile);
//
//            // 일단 로컬로 설정
//            String newProfilePath = "http://localhost:8080/" + uploadDir + fileName;
//            user.setProfilePath(newProfilePath);

        userRepository.save(user);
    }

    public boolean isScrapped(Long userId, Long newsId) {
        return scrapRepository.existsByUser_IdAndBasenews_Id(userId, newsId);
    }

    public void deleteScrap(Long userId, Long newsId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));
        Basenews news = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("없는 뉴스 ID"));

        Scrap scrap = scrapRepository.findByUserAndBasenews(user, news)
                .orElseThrow(() -> new IllegalArgumentException("스크랩된 뉴스가 아닙니다."));

        int keywordId = news.getKeyword().getId().intValue();

        user.updateKeywordInterest(keywordId, -2);
        userRepository.save(user);

        SubInterest subInterest = subInterestRepository.findByUserAndSubCategoryId(user, news.getSubCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("subInterest 없음"));

            subInterest.setScrapCount(subInterest.getScrapCount() - 1);
            subInterestRepository.save(subInterest);
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
                .isScrapped(true)
                .build());
    }
}