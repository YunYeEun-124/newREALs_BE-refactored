package newREALs.backend.repository;

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
    List<Basenews> findByIsDailyNewsTrue();


    //List<Basenews> findByCategoryAndSubCategoryOrderByIdAsc(String category, String subCategory);
    List<Basenews> findByCategoryAndSubCategoryOrderByIdAsc(Category category, SubCategory subCategory);

    List<Basenews> findByCategoryOrderByIdAsc(Category category);

    List<Basenews> findByKeywordOrderByIdAsc(Keyword keyword);

    List<Basenews> findBySubCategoryOrderByIdAsc(SubCategory subCategory);

    List<Basenews> findBySummaryIsNull();

    
    List<Basenews> findTop5ByIsDailyNewsTrueOrderByIdDesc();

    @Query("SELECT n FROM Basenews n JOIN n.keyword k WHERE k.name = :KeywordName")
    Page<Basenews> findAllByKeywordName(@Param("KeywordName") String KeywordName, Pageable pageable);

    @Query("SELECT n FROM Basenews n JOIN n.category c WHERE c.name = :CategoryName")
    Page<Basenews> findAllByCategoryName(@Param("CategoryName") String CategoryName, Pageable pageable);

    @Query("SELECT n FROM Basenews n JOIN n.subCategory s WHERE s.name = :SubCategoryName")
    Page<Basenews> findAllBySubCategoryName(@Param("SubCategoryName") String subCategoryName, Pageable pageable);

    Optional<Basenews> findFirstByCategoryNameAndIsDailyNews(String categoryName, boolean isDailyNews);

    //Page<Basenews> findAllByTitleContainingOrDescriptionContaining(String searchword,Pageable pageable);
    @Query("SELECT b FROM Basenews b WHERE b.title LIKE %:searchword% OR b.description LIKE %:searchword%")
    Page<Basenews> findAllByTitleContainingOrDescriptionContaining(String searchword, Pageable pageable);

    Optional<Basenews> findFirstByTitle(String title);

    @Query("SELECT n FROM Basenews n WHERE n.uploadDate LIKE :today%")
    List<Basenews> findAllByUploadDate(@Param("today") String today);

    List<Basenews> findAllByIsDailyNews(boolean b);
}