package newREALs.backend.config;

import newREALs.backend.domain.Category;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.repository.CategoryRepository;
import newREALs.backend.repository.KeywordRepository;
import newREALs.backend.repository.SubCategoryRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class CategoriesInitializer {

    @Bean
    public ApplicationRunner initialize(CategoryRepository categoryRepository,SubCategoryRepository subCategoryRepository, KeywordRepository keywordRepository){
        return args -> {
            System.out.println("실행 but 초기화 아직");
            if(categoryRepository.count() == 0){  //아무것도 없을때만 실행함.
                ////////////////사회 ///////////////////////
                System.out.println("초기화 실행중");
                Category society =  Category.builder().name("사회").build();
                SubCategory society_sub1 = SubCategory.builder().name("사건사고").category(society).build();
                SubCategory society_sub2 = SubCategory.builder().name("교육").category(society).build();
                SubCategory society_sub3 = SubCategory.builder().name("노동").category(society).build();
                SubCategory society_sub4 = SubCategory.builder().name("인권/복지").category(society).build();
                SubCategory society_sub5 = SubCategory.builder().name("식품/의료").category(society).build();
                SubCategory society_sub6 = SubCategory.builder().name("사회일반").category(society).build();

                Keyword society_sub1_key1 = Keyword.builder().name("교통사고").subCategory(society_sub1).category(society).build();
                Keyword society_sub1_key2 = Keyword.builder().name("범죄").subCategory(society_sub1).category(society).build();

                Keyword society_sub1_key3= Keyword.builder().name("입시").subCategory(society_sub2).category(society).build();
                Keyword society_sub1_key4 = Keyword.builder().name("교육정책").subCategory(society_sub2).category(society).build();
                Keyword society_sub1_key5 = Keyword.builder().name("학비").subCategory(society_sub2).category(society).build();

                Keyword society_sub1_key6 = Keyword.builder().name("고용").subCategory(society_sub3).category(society).build();
                Keyword society_sub1_key7 = Keyword.builder().name("최저임금").subCategory(society_sub3).category(society).build();
                Keyword society_sub1_key8 = Keyword.builder().name("노동조합").subCategory(society_sub3).category(society).build();

                Keyword society_sub1_key9 = Keyword.builder().name("여성 인권").subCategory(society_sub4).category(society).build();
                Keyword society_sub1_key10 = Keyword.builder().name("아동 복지").subCategory(society_sub4).category(society).build();
                Keyword society_sub1_key11 = Keyword.builder().name("장애인 권리").subCategory(society_sub4).category(society).build();

                Keyword society_sub1_key12 = Keyword.builder().name("식품 안전").subCategory(society_sub5).category(society).build();
                Keyword society_sub1_key13 = Keyword.builder().name("전염병").subCategory(society_sub5).category(society).build();
                Keyword society_sub1_key14 = Keyword.builder().name("헬스 케어").subCategory(society_sub5).category(society).build();

                Keyword society_sub1_key15 = Keyword.builder().name("인물").subCategory(society_sub6).category(society).build();
                Keyword society_sub1_key16 = Keyword.builder().name("환경").subCategory(society_sub6).category(society).build();
                Keyword society_sub1_key17 = Keyword.builder().name("언론").subCategory(society_sub6).category(society).build();
                categoryRepository.save(society);
                subCategoryRepository.save(society_sub1);
                subCategoryRepository.save(society_sub2);
                subCategoryRepository.save(society_sub3);
                subCategoryRepository.save(society_sub4);
                subCategoryRepository.save(society_sub5);
                subCategoryRepository.save(society_sub6);

                keywordRepository.save(society_sub1_key1);
                keywordRepository.save(society_sub1_key2);
                keywordRepository.save(society_sub1_key3);
                keywordRepository.save(society_sub1_key4);
                keywordRepository.save(society_sub1_key5);
                keywordRepository.save(society_sub1_key6);
                keywordRepository.save(society_sub1_key7);
                keywordRepository.save(society_sub1_key8);
                keywordRepository.save(society_sub1_key9);
                keywordRepository.save(society_sub1_key10);
                keywordRepository.save(society_sub1_key11);
                keywordRepository.save(society_sub1_key12);
                keywordRepository.save(society_sub1_key13);
                keywordRepository.save(society_sub1_key14);
                keywordRepository.save(society_sub1_key15);
                keywordRepository.save(society_sub1_key16);
                keywordRepository.save(society_sub1_key17);
                ////////////////경제 ///////////////
                //    경제: {



                Category economy =  Category.builder().name("경제").build();
                SubCategory economy_sub1 = SubCategory.builder().name("금융").category(economy).build();
                SubCategory economy_sub2 = SubCategory.builder().name("증권").category(economy).build();
                SubCategory economy_sub3 = SubCategory.builder().name("산업/재개").category(economy).build();
                SubCategory economy_sub4 = SubCategory.builder().name("중기/벤처").category(economy).build();
                SubCategory economy_sub5 = SubCategory.builder().name("부동산").category(economy).build();
                SubCategory economy_sub6 = SubCategory.builder().name("글로벌 경제").category(economy).build();
                SubCategory economy_sub7 = SubCategory.builder().name("생활 경제").category(economy).build();

                Keyword economy_sub1_key1 = Keyword.builder().name("금리 변동").subCategory(economy_sub1).category(economy).build();
                Keyword economy_sub1_key2 = Keyword.builder().name("금융 규제").subCategory(economy_sub1).category(economy).build();
                Keyword economy_sub1_key3 = Keyword.builder().name("은행업").subCategory(economy_sub1).category(economy).build();

                Keyword economy_sub2_key1 = Keyword.builder().name("주식 시장").subCategory(economy_sub2).category(economy).build();
                Keyword economy_sub2_key2 = Keyword.builder().name("채권").subCategory(economy_sub2).category(economy).build();
                Keyword economy_sub2_key3 = Keyword.builder().name("투자 전략").subCategory(economy_sub2).category(economy).build();

                Keyword economy_sub3_key1 = Keyword.builder().name("대기업 동향").subCategory(economy_sub3).category(economy).build();
                Keyword economy_sub3_key2 = Keyword.builder().name("산업 정책").subCategory(economy_sub3).category(economy).build();
                Keyword economy_sub3_key3 = Keyword.builder().name("기업 합병").subCategory(economy_sub3).category(economy).build();

                Keyword economy_sub4_key1 = Keyword.builder().name("스타트업").subCategory(economy_sub4).category(economy).build();
                Keyword economy_sub4_key2 = Keyword.builder().name("중소기업").subCategory(economy_sub4).category(economy).build();

                Keyword economy_sub5_key1 = Keyword.builder().name("주택 시장").subCategory(economy_sub5).category(economy).build();
                Keyword economy_sub5_key2 = Keyword.builder().name("부동산 가격").subCategory(economy_sub5).category(economy).build();
                Keyword economy_sub5_key3 = Keyword.builder().name("재개발/재건축").subCategory(economy_sub5).category(economy).build();

                Keyword economy_sub6_key1 = Keyword.builder().name("국제 무역").subCategory(economy_sub6).category(economy).build();
                Keyword economy_sub6_key2 = Keyword.builder().name("환율").subCategory(economy_sub6).category(economy).build();
                Keyword economy_sub6_key3 = Keyword.builder().name("세계 경제 전망").subCategory(economy_sub6).category(economy).build();

                Keyword economy_sub7_key1 = Keyword.builder().name("소비자 물가").subCategory(economy_sub7).category(economy).build();
                Keyword economy_sub7_key2 = Keyword.builder().name("생활비").subCategory(economy_sub7).category(economy).build();


                categoryRepository.save(economy);
                subCategoryRepository.save(economy_sub1);
                subCategoryRepository.save(economy_sub2);
                subCategoryRepository.save(economy_sub3);
                subCategoryRepository.save(economy_sub4);
                subCategoryRepository.save(economy_sub5);
                subCategoryRepository.save(economy_sub6);
                subCategoryRepository.save(economy_sub7);

                keywordRepository.save(economy_sub1_key1);
                keywordRepository.save(economy_sub1_key2);
                keywordRepository.save(economy_sub1_key3);
                keywordRepository.save(economy_sub2_key1);
                keywordRepository.save(economy_sub2_key2);
                keywordRepository.save(economy_sub2_key3);
                keywordRepository.save(economy_sub3_key1);
                keywordRepository.save(economy_sub3_key2);
                keywordRepository.save(economy_sub3_key3);
                keywordRepository.save(economy_sub4_key1);
                keywordRepository.save(economy_sub4_key2);
                keywordRepository.save(economy_sub5_key1);
                keywordRepository.save(economy_sub5_key2);
                keywordRepository.save(economy_sub5_key3);
                keywordRepository.save(economy_sub6_key1);
                keywordRepository.save(economy_sub6_key2);
                keywordRepository.save(economy_sub6_key3);
                keywordRepository.save(economy_sub7_key1);
                keywordRepository.save(economy_sub7_key2);


                ////////////////정치 ///////////////
                //    정치: {

                Category politics =  Category.builder().name("정치").build();

                SubCategory politics_sub1 = SubCategory.builder().name("대통령실").category(politics).build();
                Keyword politics_sub1_key1 = Keyword.builder().name("대통령 연설").subCategory(politics_sub1).category(politics).build();
                Keyword politics_sub1_key2 = Keyword.builder().name("청와대 정책").subCategory(politics_sub1).category(politics).build();
                Keyword politics_sub1_key3 = Keyword.builder().name("국정 운영").subCategory(politics_sub1).category(politics).build();

                SubCategory politics_sub2 = SubCategory.builder().name("국회/정당").category(politics).build();
                Keyword politics_sub2_key1 = Keyword.builder().name("법안 발의").subCategory(politics_sub2).category(politics).build();
                Keyword politics_sub2_key2 = Keyword.builder().name("정당 활동").subCategory(politics_sub2).category(politics).build();
                Keyword politics_sub2_key3 = Keyword.builder().name("국회의원 선거").subCategory(politics_sub2).category(politics).build();

                SubCategory politics_sub3 = SubCategory.builder().name("북한").category(politics).build();
                Keyword politics_sub3_key1 = Keyword.builder().name("남북 관계").subCategory(politics_sub3).category(politics).build();
                Keyword politics_sub3_key2 = Keyword.builder().name("핵 문제").subCategory(politics_sub3).category(politics).build();

                SubCategory politics_sub4 = SubCategory.builder().name("행정").category(politics).build();
                Keyword politics_sub4_key1 = Keyword.builder().name("정부 부처").subCategory(politics_sub4).category(politics).build();
                Keyword politics_sub4_key2 = Keyword.builder().name("행정 개혁").subCategory(politics_sub4).category(politics).build();
                Keyword politics_sub4_key3 = Keyword.builder().name("공공 서비스").subCategory(politics_sub4).category(politics).build();

                SubCategory politics_sub5 = SubCategory.builder().name("국방/외교").category(politics).build();
                Keyword politics_sub5_key1 = Keyword.builder().name("군사 훈련").subCategory(politics_sub5).category(politics).build();
                Keyword politics_sub5_key2 = Keyword.builder().name("국방 예산").subCategory(politics_sub5).category(politics).build();
                Keyword politics_sub5_key3 = Keyword.builder().name("외교 협상").subCategory(politics_sub5).category(politics).build();

                categoryRepository.save(politics);
                subCategoryRepository.save(politics_sub1);
                subCategoryRepository.save(politics_sub2);
                subCategoryRepository.save(politics_sub3);
                subCategoryRepository.save(politics_sub4);
                subCategoryRepository.save(politics_sub5);

                keywordRepository.save(politics_sub1_key1);
                keywordRepository.save(politics_sub1_key2);
                keywordRepository.save(politics_sub1_key3);
                keywordRepository.save(politics_sub2_key1);
                keywordRepository.save(politics_sub2_key2);
                keywordRepository.save(politics_sub2_key3);
                keywordRepository.save(politics_sub3_key1);
                keywordRepository.save(politics_sub3_key2);
                keywordRepository.save(politics_sub4_key1);
                keywordRepository.save(politics_sub4_key2);
                keywordRepository.save(politics_sub4_key3);
                keywordRepository.save(politics_sub5_key1);
                keywordRepository.save(politics_sub5_key2);
                keywordRepository.save(politics_sub5_key3);





            }

        };
    }
}

//export const CATEGORIES: Record<string, Record<string, string[]>> = Object.freeze({
//    사회: {
//        사건사고: ['교통사고', '범죄'],
//        교육: ['입시', '교육정책', '학비'],
//        노동: ['고용', '최저임금', '노동조합'],
//        '인권/복지': ['여성 인권', '아동 복지', '장애인 권리'],
//        '식품/의료': ['식품 안전', '전염병', '헬스 케어'],
//        '사회 일반': ['인물', '환경', '언론'],
//    },
//        대통령실: ['대통령 연설', '청와대 정책', '국정 운영'],
//        '국회/정당': ['법안 발의', '정당 활동', '국회의원 선거'],
//        북한: ['남북 관계', '핵 문제'],
//        행정: ['정부 부처', '행정 개혁', '공공 서비스'],
//        '국방/외교': ['군사 훈련', '국방 예산', '외교 협상'],
//    },

//    경제: {
//        금융: ['금리 변동', '금융 규제', '은행업'],
//        증권: ['주식 시장', '채권', '투자 전략'],
//        '산업/재계': ['대기업 동향', '산업 정책', '기업 합병'],
//        '중기/벤처': ['스타트업', '중소기업'],
//        부동산: ['주택 시장', '부동산 가격', '재개발/재건축'],
//        '글로벌 경제': ['국제 무역', '환율', '세계 경제 전망'],
//        '생활 경제': ['소비자 물가', '생활비'],
//    },
//});