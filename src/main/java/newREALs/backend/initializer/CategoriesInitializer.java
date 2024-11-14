package newREALs.backend.initializer;

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
                Category society =  Category.builder().name("society").build();
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


                ////////////////정치 ///////////////
                ////////////////경제 ///////////////


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
//    정치: {
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