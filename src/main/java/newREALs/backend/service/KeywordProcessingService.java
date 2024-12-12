package newREALs.backend.service;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Category;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.dto.newsInfo;
import newREALs.backend.repository.BaseNewsRepository;
import newREALs.backend.repository.CategoryRepository;
import newREALs.backend.repository.KeywordRepository;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class KeywordProcessingService {
    @PersistenceContext
    private EntityManager entityManager;

    private NewsService newsService;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.secret-key}")
    private String clientSecret;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private BaseNewsRepository baseNewsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 초당 1회 호출

    @Transactional
    public List<Basenews> processKeyword(String title, Keyword keyword, boolean isDailyNews, int display) {
        rateLimiter.acquire(); // 호출 속도 제한

        // API 호출 후 데이터 처리
        List<Basenews> createdNews = ProcessNews(title, keyword, isDailyNews, display);
        entityManager.flush(); // 즉시 데이터 반영

        // 생성된 뉴스가 없으면 로그 출력
        if (createdNews.isEmpty()) {
            System.out.println("No new Basenews created for keyword: " + keyword.getName());
        }

        return createdNews;
    }


    public List<Basenews> ProcessNews(String title, Keyword keyword, Boolean isDailyNews, int display) {
        List<Basenews> createdNews = new ArrayList<>();

        String text;
        try {
            text = URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        //API 호출
        String apiURL = "https://openapi.naver.com/v1/search/news?query=" + text + "&display="+display+"&sort=sim";    // JSON 결과
        System.out.println("naver url 검색어 : "+ title);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String responseBody = get(apiURL, requestHeaders);


        // API 응답 파싱
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
        newsInfo newsinfo = gson.fromJson(responseBody, newsInfo.class);

        if (newsinfo == null || newsinfo.getItems().isEmpty()) {
            System.out.println("No news items found for title: " + title);
            return createdNews;
        }

        for (newsInfo.Item item : newsinfo.getItems()) {
            if (!item.getLink().contains("n.news.naver.com")) {
                System.out.println("Ignoring non-Naver news: " + item.getLink());
                continue;
            }

            Optional<Basenews> existingNews = baseNewsRepository.findFirstByNewsUrl(item.getLink());
            if (existingNews.isPresent()) {
                System.out.println("News already exists: " + item.getTitle());
                if (isDailyNews) existingNews.get().checkDailyNews();
            } else {
                try {
                    // 새로운 뉴스 생성
                    Basenews bnews = Basenews.builder()
                            .title(item.getTitle().replaceAll("<[^>]*>?", "").replace("&quot;", ""))
                            .newsUrl(item.getLink())
                            .imageUrl(getArticle(item.getLink(), "#dic_area", "#img1").get(0))
                            .uploadDate(new SimpleDateFormat("yyyy-MM-dd").format(
                                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(item.getPubDate())
                            ))
                            .description(getArticle(item.getLink(), "#dic_area", "#img1").get(1))
                            .keyword(keyword)
                            .category(keyword.getCategory())
                            .subCategory(keyword.getSubCategory())
                            .isDailyNews(isDailyNews)
                            .build();

                    baseNewsRepository.save(bnews);
                    createdNews.add(bnews);

                    if (isDailyNews) break; // 데일리 뉴스는 하나만 필요
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create Basenews", e);
                }
            }
        }

        return createdNews;
    }

    //////////////////////////// 스크래핑 해서 원문 기사& 이미지 받아오기/////////////////////////
    public List<String> getArticle(String htmlUrl, String htmlId1, String htmlId2) {
        Document doc;
        String plainText = "";
        String imagePath = "";
        List<String> result = new ArrayList<>();

        try {
            // Jsoup 연결 설정
            doc = Jsoup.connect(htmlUrl)
                    .timeout(60000) // 타임아웃 60초
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // User-Agent 설정
                    .get();

            // 기사 본문 추출
            Element elements = doc.selectFirst(htmlId1);
            Element imageElements = doc.selectFirst(htmlId2);

            if (elements != null) {
                plainText = elements.text(); // HTML 태그 제거 후 텍스트만 추출
                if (imageElements != null) {
                    imagePath = imageElements.attr("data-src"); // 이미지 URL 추출
                } else {
                    System.out.println("No image found for URL: " + htmlUrl);
                    imagePath = null; // 기본 이미지 처리
                }

                result.add(imagePath);
                result.add(plainText);

            } else {
                System.out.println("Failed to find article content at: " + htmlUrl);
                throw new RuntimeException("기사 본문 추출 실패");
            }

        } catch (HttpStatusException e) {
            // HTTP 상태 코드 문제 처리
            System.out.printf("HTTP Status Error (%d) for URL: %s%n", e.getStatusCode(), htmlUrl);
            throw new RuntimeException("HTTP 호출 실패: " + e.getStatusCode(), e);
        } catch (SocketTimeoutException e) {
            // 타임아웃 문제 처리
            System.out.println("Timeout occurred while connecting to: " + htmlUrl);
            throw new RuntimeException("요청 타임아웃", e);
        } catch (IOException e) {
            // 기타 IO 에러 처리
            System.out.println("IO Error while fetching article: " + htmlUrl);
            throw new RuntimeException("네트워크 문제 발생", e);
        }

        System.out.println("Successfully 뉴스 원문 가져오기 성공: " + htmlUrl);
        return result;
    }



    public String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        int retries = 3; // 최대 재시도 횟수
        while (retries > 0) {
            try {
                con.setRequestMethod("GET");
                for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return readBody(con.getInputStream());
                } else if (responseCode == 429) { // 호출 제한 초과
                    System.out.println("Rate limit hit. Retrying...");
                    Thread.sleep(1000); // 1초 대기 후 재시도
                } else {
                    return readBody(con.getErrorStream());
                }
            } catch (IOException | InterruptedException e) {
                retries--;
                if (retries == 0) throw new RuntimeException("API 요청 실패", e);
            } finally {
                con.disconnect();
            }
        }
        throw new RuntimeException("API 요청 실패");
    }





    private  HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }


    private  String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);


        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();


            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }


            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }
}
