package newREALs.backend.news.service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.collections4.ListUtils;
import newREALs.backend.news.domain.Basenews;
import newREALs.backend.news.domain.Keyword;
import newREALs.backend.news.repository.BaseNewsRepository;
import newREALs.backend.news.repository.CategoryRepository;
import newREALs.backend.news.repository.KeywordRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;

import java.util.*;

@Service
public class GetNaverNews {
    @Autowired
    private  KeywordRepository keywordRepository;

    @Autowired
    private BaseNewsRepository baseNewsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ChatGPTService chatGPTService;

    private final NewsService newsService;
    private final KeywordProcessingService keywordProcessingService;

    @PersistenceContext
    private EntityManager entityManager;

    public GetNaverNews(ChatGPTService chatGPTService, NewsService newsService, ArticleProcessingService articleProcessingService, KeywordProcessingService keywordProcessingService) {
        this.chatGPTService = chatGPTService;
        this.newsService = newsService;
        this.keywordProcessingService = keywordProcessingService;
    }




    @Scheduled(cron = "0 10 06 ? * *")
    public void getBasenews() {
        System.out.println("getBasenews in");
        List<List<Keyword>> keywords =ListUtils.partition(keywordRepository.findAll(),10); //key word 다 불러와

        if (keywords.isEmpty()) {
            System.out.println("no keywords ");
            return;
        }

        for (List<Keyword> keywordList : keywords) { //검색 for문으로 키워드 돌아가면서 실행시키
             for(Keyword keyword : keywordList){
                 try {
                     keywordProcessingService.processKeyword(keyword.getName(),keyword,false,1);
                     Thread.sleep(1000); // 1초 대기
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                     System.out.println("Thread interrupted during delay");
                 }
             }
            // 배치 간 대기
            try {
                Thread.sleep(2000); // 각 배치 처리 후 2초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Batch delay interrupted");
            }
          

        }

        newsService.automaticBaseProcess();

    }



    //매일 아침마다 하루 한 번 실행

    @Scheduled(cron = "0 48 01 ? * *")
    public void getDailynews(){
        System.out.println("getDailynews");
        List<Basenews> previousDailyNews = baseNewsRepository.findAllByIsDailyNews(true);

        if (!previousDailyNews.isEmpty()) {
            for (Basenews basenews : previousDailyNews) {
                basenews.cancelDailyNews(); // 상태 변경
            }
            baseNewsRepository.saveAll(previousDailyNews);
        }
        System.out.println("now daily news size :"+baseNewsRepository.findAllByIsDailyNews(true).size());
        String[] category = {"사회","경제","정치"};

        int pageNum = 102; int limit = 2;

        for(int i=0;i<3;i++){ //사회(102), 경제(101) 정치(100) 순서

            String currentCategory = category[i];

            if(Objects.equals(currentCategory, "정치")){
                limit = 1;
                pageNum = 100;
            }else if(Objects.equals(currentCategory, "사회")){
                limit = 2;
                pageNum = 101;
            }else {
                limit = 2;
                pageNum = 102;
            }

            List<String> titleKeywordList = mapTitleKeyword(pageNum,limit,currentCategory);//카테고리마다 데일리뉴스 만들 타이틀 목록

            //Dailynews mapping : 이미 있는 기사면 isDailynews = t 로 수정처리 .
            StringTokenizer st;
            for(int j=0;j<limit;j++){

                st = new StringTokenizer(titleKeywordList.get(j),":");
                System.out.println("this is a title"+titleKeywordList.get(j));
                String title = st.nextToken().trim(); //공백, 구분자 제거
                String k = st.nextToken().trim();
                System.out.println("title , keyword from gpt : "+title+"."+k);
                Optional<Keyword> keyword = keywordRepository.findByName(k);

                if(keyword.isPresent()){
                    System.out.println("추출한 키워드 : "+keyword.get().getName());
                    // 딜레이 추가
                    try {
                        List<Basenews> createdNews = keywordProcessingService.processKeyword(title,keyword.get(),true,3);
                        Thread.sleep(1000); // 1초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Thread interrupted during delay");
                    }
                }else{
                    System.out.println("can't find daily news keyword");
                    return;
                }

            }

        }

        newsService.automaticBaseProcess(); //내용 채우기
        newsService.automaticDailyProcess(); // 퀴즈, 인사이트 생성

    }


    public  List<String> mapTitleKeyword(int pageNum,int limit,String category){

        String keywordList = String.join(",",keywordRepository.findAllByCategory_Name(category));

        //사회 2, 정치 1, 경제 2
        String titleList = String.join("\n",getDailyTitles("https://news.naver.com/section/"+pageNum));

        List<Map<String, String>> titleMessages = new ArrayList<>();

        titleMessages.add(Map.of("role", "system", "content", "입문자수준에서 "+category+"면에서 중요한 기사 "+limit+"개를 선별해야해."));
        titleMessages.add(Map.of("role", "user", "content", "다음 타이틀 리스트에서 "+category+"에서 중요한 타이틀 "+limit+"개만 골라야돼."+titleList +
                "0. 가장 중요하다고 생각되는 타이틀을 골라야해."+
                "1. 평소에 뉴스를 읽지 않는 10대 20대들도 꼭 알아야하는 뉴스라고 생각되는 것을 선별" +
                "2. "+category+" 카테고리에 적합해야함" +
                "3. 해당 타이틀의 뉴스로 입문자를 위한 퀴즈 생성까지 고려해서 골라" +
                "다음은 고른 타이틀과 카테고리를 한개씩 매핑해야해. 카테고리는 다음과 같아. " + keywordList
                +"이 카테고리 외에 다른 카테고리를 매핑시키면 안되고,정확한 이름,띄어쓰기도 지켜야해" +
                "최종 결과물 형식은 " +
                "타이틀 : 카테고리 이걸 꼭 지켜야해 " +
                " 예시는 다음과 같아 . " +
                "\n 대통령, 캐나다 총리와 정상회담…방산 등 포괄적 안보협력 확대 : 대통령 연설 \n "+
                "\n 넘버링도 하지말고 쌍따옴표로 감싸도 안돼. 무조건 내가 말한 타이틀 : 카테고리가 한줄씩 출력 "
        ));

        String titleKeyword = (String) chatGPTService.generateContent(titleMessages).get("text");
        StringTokenizer st = new StringTokenizer(titleKeyword,"\n");

        List<String> titleKeywordList = new ArrayList<>();


        while(st.hasMoreTokens())
            titleKeywordList.add(st.nextToken());


        return titleKeywordList;


    }

    public  List<String> getDailyTitles(String htmlUrl){
        Document doc;
        String title = "";

        List<String> titles = new ArrayList<>();
        try{
            String url = htmlUrl;
            doc = Jsoup.connect(url)
                .timeout(60000) // 타임아웃 60초
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // User-Agent 추가
                .get();

            Elements elements = doc.select(".sa_text_strong");

            for(Element element : elements){
                title = element.text();
                titles.add(title);
            }

        }catch (IOException e){
            throw new RuntimeException("타이틀 못 뽑았네요~~~~~", e);
        }

        return titles;
    }



}

