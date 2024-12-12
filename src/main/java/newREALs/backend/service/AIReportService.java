package newREALs.backend.service;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.SubInterest;
import newREALs.backend.repository.SubInterestRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIReportService {

    private final SubInterestRepository subInterestRepository;
    private final S3Service s3Service;

    /**
     * 모든 사용자에 대한 관심사 분석 및 레포트 생성
     * @return S3 URL 리스트 (업로드된 파일)
     */
    public List<String> generateReports() {
        List<Accounts> users = subInterestRepository.findAllUsers(); // 모든 사용자 가져오기
        List<String> uploadedFiles = new ArrayList<>();

        for (Accounts user : users) {
            try {
                // 개별 사용자 레포트 생성 및 업로드
                String s3Url = createAndUploadUserReport(user);
                uploadedFiles.add(s3Url);
            } catch (Exception e) {
                System.err.println("사용자 " + user.getId() + "의 레포트 생성 실패: " + e.getMessage());
            }
        }

        return uploadedFiles;
    }

    /**
     * 개별 사용자의 관심사 데이터를 CSV로 생성하고 S3에 업로드
     * @param user 사용자 객체
     * @return 업로드된 S3 URL
     */
    private String createAndUploadUserReport(Accounts user) throws IOException {
        List<SubInterest> interests = subInterestRepository.findByUserId(user.getId());
        File csvFile = createCsvFile(user, interests);

        // S3에 파일 업로드
        String s3Url = s3Service.uploadReportFile(csvFile, "reports/" + user.getId());

        // 로컬 파일 삭제
        s3Service.deleteLocalFile(csvFile);

        return s3Url;
    }

    /**
     * 관심사 데이터를 CSV 파일로 생성
     * @param user 사용자 객체
     * @param interests 관심사 데이터 리스트
     * @return 생성된 CSV 파일
     */
    private File createCsvFile(Accounts user, List<SubInterest> interests) throws IOException {
        String fileName = "user_report_" + user.getId() + ".csv";
        File csvFile = new File(fileName);

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            // CSV 헤더 작성
            writer.writeNext(new String[]{"User ID", "SubCategory", "Count", "QuizCount", "ScrapCount", "CommentCount", "AttCount"});

            // 데이터 작성
            for (SubInterest interest : interests) {
                writer.writeNext(new String[]{
                        String.valueOf(user.getId()),
                        interest.getSubCategory().getName(),
                        String.valueOf(interest.getCount()),
                        String.valueOf(interest.getQuizCount()),
                        String.valueOf(interest.getScrapCount()),
                        String.valueOf(interest.getCommentCount()),
                        String.valueOf(interest.getAttCount())
                });
            }
        }

        return csvFile;
    }
}
