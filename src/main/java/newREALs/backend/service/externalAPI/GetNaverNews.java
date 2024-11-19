package newREALs.backend.service.externalAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.transaction.Transactional;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Category;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.dto.newsInfo;
import newREALs.backend.repository.BaseNewsRepository;
import newREALs.backend.repository.CategoryRepository;
import newREALs.backend.repository.KeywordRepository;
import newREALs.backend.service.ChatGPTService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GetNaverNews {
    @Autowired
    private  KeywordRepository keywordRepository;

    @Autowired
    private BaseNewsRepository baseNewsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ChatGPTService chatGPTService;

    private static final Dotenv dotenv=Dotenv.load();
    private static final String clientId=dotenv.get("NAVER_API_CLIENTID");
    private static final String clientSecret=dotenv.get("NAVER_API_SECRETKEY");
    private static newsInfo newsinfo;

    public GetNaverNews(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }


    @Scheduled(cron = "0 36 09 ? * *")
    @Transactional
    public void getBasenews() {
        List<Keyword> keywords = keywordRepository.findAll(); //key word 다 불러와

        if (keywords.isEmpty()) {
            System.out.println("no keywords ");
            return;
        }

        for (Keyword keyword : keywords) { //검색 for문으로 키워드 돌아가면서 실행시키
            ProcessNews(keyword.getName(), keyword, false,5);
        }

    }


    //매일 아침마다 하루 한 번 실행
    @Scheduled(cron = "0 36 09 ? * *")
    @Transactional
    public void getDailynews(){

        //전날 데일리 뉴스 되돌리기
        List<Basenews> previousDailynews = baseNewsRepository.findAllByIsDailyNews(true);
        if(!previousDailynews.isEmpty()) {
            for (Basenews basenews : previousDailynews) basenews.cancelDailyNews();
        }
        List<Category> categoryList = categoryRepository.findAll();

        int pageNum = 102; int limit = 2;

        for(int i=0;i<categoryList.size();i++){ //사회(102), 경제(101) 정치(100) 순서

            Category currentCategory = categoryList.get(i);
            if(Objects.equals(currentCategory.getName(), "politics")){
                limit = 1;
                pageNum = 100;
            }else if(Objects.equals(currentCategory.getName(), "economy")){
                limit = 2;
                pageNum = 101;
            }else {
                limit = 2;
                pageNum = 102;
            }

            List<String> titleKeywordList = mapTitleKeyword(pageNum,limit,currentCategory);//카테고리마다 데일리뉴스 만들 타이틀 목록

            //Dailynews mapping : 이미 있는 기사면 isDailynews = t 로 수정처리 .
            StringTokenizer st;
            for(int j=0;j<titleKeywordList.size();j++){

                st = new StringTokenizer(titleKeywordList.get(j),":");
                String title = st.nextToken().trim(); //공백, 구분자 제거
                String k = st.nextToken().trim();
                Optional<Keyword> keyword = keywordRepository.findByName(k);

                if(keyword.isPresent()){
                    System.out.println("추출한 키워드 : "+keyword.get().getName());
                    ProcessNews(title,keyword.get(),true,4);
                }else{
                    System.out.println("can't find daily news keyword");

                }
            }
        }




    }


    public  List<String> mapTitleKeyword(int pageNum,int limit,Category category){

        String keywordList = String.join(",",keywordRepository.findAllByCategory_Name(category.getName()));
        System.out.println("keywordlist : "+keywordList);
        //사회 2, 정치 1, 경제 2
        String titleList = String.join("\n",getDailyTitles("https://news.naver.com/section/"+pageNum));

        List<Map<String, String>> titleMessages = new ArrayList<>();
        titleMessages.add(Map.of("role", "system", "content", "입문자수준에서 사회면에서 중요한 기사 "+limit+"개를 선별해야해."));
        titleMessages.add(Map.of("role", "user", "content", "다음 타이틀 리스트에서 사회면에서 중요한 타이틀 "+limit+"개를 골라줘"+titleList +
                "기준은 다음과 같다. " +
                "0. 가장 중요하다고 생각되는 타이틀을 골라야해."+
                "1. 평소에 뉴스를 읽지 않는 10대 20대들도 꼭 알아야하는 뉴스라고 생각되는 것을 선별해야하고" +
                "2. "+category.getName()+" 카테고리에 적합한 걸 골라야해" +
                "3. 해당 타이틀의 뉴스로 입문자를 위한 퀴즈 만들어야하기 때문에 이를 고려해서 골라야해" +
                "4. 특정 지역에 관한 내용은 제외해줘 " +

                "다음은 고른 타이틀과 카테고리를 한개씩 매핑해야해. 카테고리는 다음과 같아. " + keywordList
                +"최종 결과물 형식은 " +
                "타이틀 : 카테고리 이걸 꼭 지켜야해 " +
                " 예시는 다음과 같아 . 대통령, 캐나다 총리와 정상회담…방산 등 포괄적 안보협력 확대 : 대통령 연설 \n "+
                "\n 이 형식에 넘버링도 하지말고 쌍따옴표로 감싸도 안돼. 무조건 내가 말한 타이틀 : 카테고리가 한줄씩 출력만하면돼 "
        ));

        String titleKeyword = (String) chatGPTService.generateContent(titleMessages).get("text");

        System.out.println("gpt result");

        StringTokenizer st = new StringTokenizer(titleKeyword,"\n");
        List<String> titleKeywordList = new ArrayList<>();
        int i=0;
        while(st.hasMoreTokens()){
            titleKeywordList.add(st.nextToken());
            System.out.println(titleKeywordList.get(i++));
        }

        return titleKeywordList;


    }

    public  List<String> getDailyTitles(String htmlUrl){
        Document doc;
        String title = "";

        List<String> titles = new ArrayList<>();
        try{
            String url = htmlUrl;
            doc =  Jsoup.connect(url).get();
            Elements elements = doc.select(".sa_text_strong");

            for(Element element : elements){
                title = element.text();
                //  System.out.println(element.text());
                titles.add(title);
            }

        }catch (IOException e){
            throw new RuntimeException("뉴스 원문 못 가져왔대요~~", e);
        }

        return titles;
    }




    public  void ProcessNews(String title,Keyword keyword,Boolean isDailyNews,int display) {

        String text;
        try {
            text = URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        String apiURL = "https://openapi.naver.com/v1/search/news?query=" + text + "&display="+display+"&sort=sim";    // JSON 결과
        System.out.println("naver url 검색어 : "+ title);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String responseBody = get(apiURL, requestHeaders);

        try {

            Gson gson = new GsonBuilder() //추가 필드 생겨서 예외처리해야함.
                    .excludeFieldsWithoutExposeAnnotation()
                    .setPrettyPrinting()
                    .create();

            newsinfo = gson.fromJson(responseBody, newsInfo.class);

            if (newsinfo == null || newsinfo.getItems().isEmpty() ) {
                System.out.println("newsinfo or newsinfo.getItems() is null");
                return;
            }

            System.out.println("=====================newsinfo 사이즈 =============================:"+newsinfo.getItems().size());

            for (newsInfo.Item item : newsinfo.getItems()) {

                if (!item.getLink().contains("n.news.naver.com")) {
                    System.out.println("this is not naver news.");
                    continue;
                }
                Optional<Basenews> basenews = baseNewsRepository.findFirstByTitle(item.getTitle());

                if(basenews.isPresent()){
                    System.out.println(item.getTitle() + "is already in it.");
                    if(isDailyNews){
                        basenews.get().checkDailyNews();//이미 있는 뉴스를 데일리뉴스로 만든다.
                    }
                    return;
                }else{
                    List<String> origin = getArticle(item.getLink(),"#dic_area","#img1"); //newsinfo 원문,이미지링크 필드 채우기.
                    SubCategory sub = keyword.getSubCategory();
                    Category category = keyword.getCategory();

                    try {
                        Basenews bnews = Basenews.builder()
                                .title(item.getTitle())
                                .newsUrl(item.getLink())
                                .imageUrl(origin.get(0))
                                .uploadDate(item.getPubDate())
                                .description(origin.get(1))
                                .keyword(keyword)
                                .subCategory(sub)
                                .category(category)
                                .isDailyNews(isDailyNews)
                                .build();
                        baseNewsRepository.save(bnews);
                        System.out.println("result : "+bnews.getDescription());
                        //System.out.println(item.getLink());
                        if(isDailyNews) break; //하나씩만있으면되니까 for loop 나와~
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("basenews 생성 실패", e);
                    }
                    //현재 기준으로 채울수있는 필드만 채워서 보내기, 추후 원문전체기사를 우선 description에 넣어둠.
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("json error", e);
        }

    }

    //////////////////////////// 스크래핑 해서 원문 기사& 이미지 받아오기/////////////////////////
    public List<String> getArticle(String htmlUrl,String htmlId1, String htmlId2){
        Document doc;
        String plainText = "";
        String imagePath = "";
        List<String> set = new ArrayList<>();
        try{
            String url = htmlUrl;
            doc =  Jsoup.connect(url).get();

            //기사 데려오기v & 기사사진
            Element elements = doc.selectFirst(htmlId1);
            Element imageElements = doc.selectFirst(htmlId2);

            if(elements != null){
                plainText = elements.text(); //각종 태그 없애고 텍스트만 가져오기
                if(imageElements != null){
                    imagePath = imageElements.attr("data-src");//태그 안 정보 가져오기
                }else{ //default image
                    imagePath = "https://imgnews.pstatic.net/image/469/2024/11/05/0000831587_001_20241105174007220.jpg?type=w860";
                }

                set.add(imagePath);
                set.add(plainText);

            }else {
                //  elements = doc.selectFirst("#_article_");
                System.out.println("추출 안된 주소 : "+url);
                throw  new RuntimeException("기사 추출 못했음. ");
            }

        }catch (IOException e){
            throw new RuntimeException("뉴스 원문 못 가져왔대요~~", e);
        }
        //  System.out.println("get article 기사 추출 성공");
        return set;
    }

    ////////////////////////////네이버 뉴스 연동 메서드///////////////////

    public  String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 오류 발생
                System.out.println("에러입");
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
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