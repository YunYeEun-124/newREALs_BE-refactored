package newREALs.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.*;
import newREALs.backend.dto.NewsDetailDto;
import newREALs.backend.dto.SimpleNewsDto;
import newREALs.backend.dto.TermDetailDto;
import newREALs.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsDetailService {
    private final BaseNewsRepository basenewsRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final LikesRepository likesRepository;
    private final SubInterestRepository subInterestRepository;
    private final ClickRepository clickRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final KeywordRepository keywordRepository;


    //뉴스 상세페이지 조회 메서드
    @Transactional
    public NewsDetailDto getNewsDetail(Long basenewsId, Long userId, String cate, String subCate, String keyword){
        Basenews basenews=basenewsRepository.findById(basenewsId)
                .orElseThrow(()->new IllegalArgumentException("Invalid news ID"));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Invalid user ID"));

        //cate, subCate, keyword중 하나는 무조건 받아야함
        if(cate==null&&subCate==null&&keyword==null){
           //오류 던짐
            throw new IllegalArgumentException("카테고리, 서브카테고리, 키워드 중 한가지를 입력하세요.");
        }

        //조회수 증가
        increaseViewCount(basenews,user);

        //basenews를 newsdetailDTO로 변환
        NewsDetailDto newsDetailDto=new NewsDetailDto(basenews);

        //용어 목록도 DTO로 변환
        List<TermDetailDto> termList=basenews.getTermList().stream()
                .map(TermDetailDto::new)
                .collect(Collectors.toList());
        newsDetailDto.setTermList(termList);

        //유저 스크랩여부 확인
        Optional<Scrap> isScrapped=scrapRepository.findByUserAndBasenews(user,basenews);
        boolean b=isScrapped.isPresent();
        newsDetailDto.setScrapped(b);


        //이전, 다음 뉴스 받아오기~!~!
        List<Basenews> sortedNews=fetchSortedNews(cate,subCate,keyword,basenews);
        Basenews prevNews=findPrevNews(sortedNews,basenewsId);
        Basenews nextNews=findNextNews(sortedNews,basenewsId);


        if(prevNews!=null){
            newsDetailDto.setPrevNews(new SimpleNewsDto(prevNews.getId(),prevNews.getTitle()));
        }
        if (nextNews != null) {
            newsDetailDto.setNextNews(new SimpleNewsDto(nextNews.getId(), nextNews.getTitle()));
        }

        //이전,다음 기사 이동 버튼 눌렀을때 같이 넘겨줄 wherePageFrom 설정
        newsDetailDto.setWherePageFrom(determinWherePageFrom(keyword,subCate,cate));

        return newsDetailDto;
    }

    private String determinWherePageFrom(String keyword, String subCate, String cate) {
        if(keyword!=null) return keyword;
        if(subCate!=null) return subCate;
        return cate;
    }

    // 같은 카테고리/서브카테고리/키워드에 속하는 Basenews 리스트 생성
    private List<Basenews> fetchSortedNews(String cate, String subCate,String key,Basenews basenews) {

        //키워드 존재 : 메인페이지에서 온것
        if(key!=null&&!key.isEmpty()){
            Keyword keyword=keywordRepository.findByName(key)
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 keyword입니다.:" +key));
            if(basenews.getKeyword()!=keyword){throw new IllegalArgumentException("뉴스 키워드와 파라미터가 일치하지 않습니다.");}
            return basenewsRepository.findByKeywordOrderByIdAsc(keyword);
        }
        //키워드 없고 서브카테고리 존재 : 소카테고리 페이지에서 온것
        else if (subCate != null && !subCate.isEmpty()) {
            SubCategory subCategory = subCategoryRepository.findByName(subCate)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Subcategory입니다.: " + subCate));
            if(basenews.getSubCategory()!=subCategory){throw new IllegalArgumentException("뉴스 소카테고리와 파라미터 불일치");}
            return basenewsRepository.findBySubCategoryOrderByIdAsc(subCategory);
        }
        //큰 카테고리만 존재 : 큰 카테고리 페이지에서 온 것
        else {
            Category category = categoryRepository.findByName(cate)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Category입니다.:" + cate));
            if(basenews.getCategory()!=category){throw new IllegalArgumentException("뉴스 카테고리와 파라미터 불일치");}
            return basenewsRepository.findByCategoryOrderByIdAsc(category);
        }
    }

    //이전 기사 찾기
    private Basenews findPrevNews(List<Basenews> sortedNews, Long curId) {
        for (int i = 0; i < sortedNews.size(); i++) {
            if (sortedNews.get(i).getId().equals(curId) && i > 0) {
                return sortedNews.get(i - 1); // 현재 뉴스 이전의 뉴스 반환
            }
        }
        return null; // 이전 뉴스가 없을 경우
    }

    //다음 기사 찾기
    private Basenews findNextNews(List<Basenews> sortedNews, Long curId) {
        for (int i = 0; i < sortedNews.size(); i++) {
            if (sortedNews.get(i).getId().equals(curId) && i < sortedNews.size() - 1) {
                return sortedNews.get(i + 1); // 현재 뉴스 다음의 뉴스 반환
            }
        }
        return null; // 다음 뉴스가 없을 경우
    }


    //조회수 증가 메서드
    @Transactional
    public void increaseViewCount(Basenews basenews, Accounts user){
        Optional<Click> click=clickRepository.findByUserAndBasenews(user,basenews);
        if(!click.isPresent()){
            //처음 클릭하는거면
            Click c=new Click(user,basenews);
            clickRepository.save(c);
            basenews.setViewCount(basenews.getViewCount()+1);
            basenewsRepository.save(basenews);
        }else{
            //내가 이걸 몇번 봤는지만 카운트.. 뉴스자체의 조회수는 안올라감
            Click c= click.get();
            c.setCount(c.getCount()+1);
        }
    }



}
