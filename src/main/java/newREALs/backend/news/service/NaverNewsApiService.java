package newREALs.backend.news.service;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import newREALs.backend.news.domain.Basenews;
import newREALs.backend.news.domain.Keyword;
import newREALs.backend.news.dto.newsInfo;
import newREALs.backend.news.repository.BaseNewsRepository;
import newREALs.backend.news.repository.CategoryRepository;
import newREALs.backend.news.repository.KeywordRepository;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NaverNewsApiService {
    @PersistenceContext
    private EntityManager entityManager;

    @Value("${naver.api.client-id}")
    private String clientId;
    @Value("${naver.api.secret-key}")
    private String clientSecret;

    @Autowired
    private BaseNewsRepository baseNewsRepository;



    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 초당 1회 호출


    @Transactional
    public void executeFullNewsFlow(String title, Keyword keyword, boolean isDailyNews, int display) {
        rateLimiter.acquire(); // 호출 속도 제한

        String naverApiResponseBody =  fetchNewsItemsFromNaver(title,display); //1. naverapi로 뉴스 item 리스트 받아옴
        parseAndStoreNews(naverApiResponseBody,title,keyword,isDailyNews); //2. 받아온 리스트로
        
        entityManager.flush(); // 즉시 데이터 반영


    }

    /*
    * 불러온 뉴스 정보로 뉴스 원문 크롤링 후 중복 체크 후 저장
    * ------------------------------------------------------------
    * case 1 . isDailyNews = true 인 경우, 데일리 뉴스로 호출했을 때 |
    * case 2.  isDailyNews = false 인 경우, 일반 뉴스로 호출했을 때  |
    *
    * */

    private void parseAndStoreNews(String naverApiResponseBody,String title, Keyword keyword, Boolean isDailyNews){
        // API 응답 파싱
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();

        newsInfo newsinfo = gson.fromJson(naverApiResponseBody, newsInfo.class);

        if (newsinfo == null || newsinfo.getItems().isEmpty()) {
            System.out.println("No news items found for title: " + title);
            return;

        }

        for (newsInfo.Item item : newsinfo.getItems()) {
            if (!item.getLink().contains("n.news.naver.com")) continue; //naver 기사 아닌 경우 PASS

            Optional<Basenews> existingNews = baseNewsRepository.findFirstByNewsUrl(item.getLink());

            if (existingNews.isPresent()) {
                if (isDailyNews) existingNews.get().checkDailyNews();
                continue;
            }
            
            try {
                Basenews basenews = createBaseNews(item, keyword,isDailyNews);
                baseNewsRepository.save(basenews);
                if (isDailyNews) break; // 데일리 뉴스는 하나만 필요
            } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create Basenews", e);

            }
        }

    }

    /*
        Naver Search API 연동하여 뉴스 정보 불러옵니다.
    */
    private String fetchNewsItemsFromNaver(String title, int display) {


        String text;
        try {
            text = URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        //API 호출
        String apiURL = "https://openapi.naver.com/v1/search/news?query=" + text + "&display="+display+"&sort=sim";    // JSON 결과
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        return get(apiURL, requestHeaders); //뉴스 정보 리스트


    }

    /* 
        스크래핑 해서 원문 기사& 이미지 받아오기
    */
    private List<String> getNewsContentAndImage(String htmlUrl, String htmlId1, String htmlId2) {

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
                throw new RuntimeException("기사 본문 추출 실패");
            }

        } catch (HttpStatusException e) { // HTTP 상태 코드 문제 처리
            throw new RuntimeException("HTTP 호출 실패: " + e.getStatusCode(), e);
        } catch (SocketTimeoutException e) { // 타임아웃 문제 처리
            throw new RuntimeException("요청 타임아웃", e);
        } catch (IOException e) { // 기타 IO 에러 처리
            throw new RuntimeException("네트워크 문제 발생", e);
        }


        return result;
    }



    private  String get(String apiUrl, Map<String, String> requestHeaders) {
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
            StringBuilder naverApiResponseBody = new StringBuilder();


            String line;
            while ((line = lineReader.readLine()) != null) {
                naverApiResponseBody.append(line);
            }


            return naverApiResponseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    private Basenews createBaseNews(newsInfo.Item item,Keyword keyword,Boolean isDailyNews) throws ParseException {
        List<String> contentAndImage = getNewsContentAndImage(item.getLink(), "#dic_area", "#img1");

        return Basenews.builder()
                .title(item.getTitle().replaceAll("<[^>]*>?", "").replace("&quot;", ""))
                .newsUrl(item.getLink())
                .imageUrl(contentAndImage.get(0))
                .uploadDate(new SimpleDateFormat("yyyy-MM-dd").format(
                        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(item.getPubDate())
                ))
                .description(contentAndImage.get(1))
                .keyword(keyword)
                .category(keyword.getCategory())
                .subCategory(keyword.getSubCategory())
                .isDailyNews(isDailyNews)
                .build();
    }
}
