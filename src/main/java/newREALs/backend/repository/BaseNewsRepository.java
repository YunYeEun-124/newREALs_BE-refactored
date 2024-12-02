package newREALs.backend.repository;//package newREALs.backend.repository;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Category;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaseNewsRepository extends JpaRepository<Basenews,Long> {

    List<Basenews> findByCategoryOrderByIdAsc(Category category);

    List<Basenews> findByKeywordOrderByIdAsc(Keyword keyword);

    List<Basenews> findBySubCategoryOrderByIdAsc(SubCategory subCategory);


    @Query("SELECT b FROM Basenews b WHERE b.summary IS NULL")
    List<Basenews> findBySummaryIsNull();


    List<Basenews> findTop5ByIsDailyNewsTrueOrderByIdDesc();

    @Query("SELECT n FROM Basenews n JOIN n.keyword k WHERE k.name = :KeywordName")
    Page<Basenews> findAllByKeywordName(@Param("KeywordName") String KeywordName, Pageable pageable);

    @Query("SELECT n FROM Basenews n JOIN n.category c WHERE c.name = :CategoryName")
    Page<Basenews> findAllByCategoryName(@Param("CategoryName") String CategoryName, Pageable pageable);

    @Query("SELECT n FROM Basenews n JOIN n.subCategory s WHERE s.name = :SubCategoryName")
    Page<Basenews> findAllBySubCategoryName(@Param("SubCategoryName") String subCategoryName, Pageable pageable);

    @Query("SELECT b FROM Basenews b WHERE b.title LIKE %:searchword% OR b.description LIKE %:searchword%")
    Page<Basenews> findAllByTitleContainingOrDescriptionContaining(String searchword, Pageable pageable);

    @Query("SELECT bn FROM Basenews bn  WHERE bn.keyword.name IN :keywordsName")
    Page<Basenews> findAllByKeywords(@Param("keywordsName") List<String> keywordsId, Pageable pageable);

    List<Basenews> findAllByIsDailyNews(boolean b);

    Optional<Basenews> findFirstByCategoryAndIsDailyNews(Category category, boolean b);

    Optional<Basenews> findFirstByNewsUrl(String link);


}


//@Repository
//public interface BaseNewsRepository extends JpaRepository<Basenews, Long> {
//
//    List<Basenews> findByCategoryAndSummaryIsNotNullOrderByIdAsc(Category category);
//
//    List<Basenews> findByKeywordAndSummaryIsNotNullOrderByIdAsc(Keyword keyword);
//
//    List<Basenews> findBySubCategoryAndSummaryIsNotNullOrderByIdAsc(SubCategory subCategory);
//
//    @Query("SELECT b FROM Basenews b WHERE b.summary IS NULL")
//    List<Basenews> findBySummaryIsNull();
//
//    List<Basenews> findTop5ByIsDailyNewsTrueAndSummaryIsNotNullOrderByIdDesc();
//
//    @Query("SELECT n FROM Basenews n JOIN n.keyword k WHERE k.name = :KeywordName AND n.summary IS NOT NULL")
//    Page<Basenews> findAllByKeywordNameAndSummaryIsNotNull(@Param("KeywordName") String KeywordName, Pageable pageable);
//
//    @Query("SELECT n FROM Basenews n JOIN n.category c WHERE c.name = :CategoryName AND n.summary IS NOT NULL")
//    Page<Basenews> findAllByCategoryNameAndSummaryIsNotNull(@Param("CategoryName") String CategoryName, Pageable pageable);
//
//    @Query("SELECT n FROM Basenews n JOIN n.subCategory s WHERE s.name = :SubCategoryName AND n.summary IS NOT NULL")
//    Page<Basenews> findAllBySubCategoryNameAndSummaryIsNotNull(@Param("SubCategoryName") String subCategoryName, Pageable pageable);
//
//    @Query("SELECT b FROM Basenews b WHERE (b.title LIKE %:searchword% OR b.description LIKE %:searchword%) AND b.summary IS NOT NULL")
//    Page<Basenews> findAllByTitleContainingOrDescriptionContainingAndSummaryIsNotNull(@Param("searchword") String searchword, Pageable pageable);
//
//    @Query("SELECT bn FROM Basenews bn JOIN bn.keyword k WHERE k.name IN :keywordsName AND bn.summary IS NOT NULL")
//    Page<Basenews> findAllByKeywordsAndSummaryIsNotNull(@Param("keywordsName") List<String> keywordsId, Pageable pageable);
//
//    List<Basenews> findAllByIsDailyNewsAndSummaryIsNotNull(boolean b);
//
//    Optional<Basenews> findFirstByCategoryAndIsDailyNewsAndSummaryIsNotNull(Category category, boolean b);
//
//    Optional<Basenews> findFirstByNewsUrlAndSummaryIsNotNull(String link);
//}
