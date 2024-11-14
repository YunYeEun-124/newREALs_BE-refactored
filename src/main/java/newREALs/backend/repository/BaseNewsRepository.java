package newREALs.backend.repository;

import newREALs.backend.domain.Basenews;
import org.springframework.data.annotation.ReadOnlyProperty;
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

    //List<Basenews> findAllBySubcategoryId(Long subCategoryId);

    @Query("SELECT n FROM Basenews n JOIN n.category c WHERE c.name = :CategoryName")
    Page<Basenews> findAllByCategoryName(@Param("CategoryName") String CategoryName, Pageable pageable);

    @Query("SELECT n FROM Basenews n JOIN n.subCategory s WHERE s.name = :SubCategoryName")
    Page<Basenews> findAllBySubCategoryName(@Param("SubCategoryName") String subCategoryName,Pageable pageable);

    Optional<Basenews> findFirstByCategoryNameAndIsDailyNews(String categoryName, boolean isDailyNews);

    //Page<Basenews> findAllByTitleContainingOrDescriptionContaining(String searchword,Pageable pageable);
    @Query("SELECT b FROM Basenews b WHERE b.title LIKE %:searchword% OR b.description LIKE %:searchword%")
    Page<Basenews> findAllByTitleContainingOrDescriptionContaining(String searchword, Pageable pageable);

    Optional<Basenews> findFirstByTitle(String title);

    // Basenews랑 Scrap 조인 -> 유저 id로 isScrap이 true인 거 불러오기
    @Query("SELECT b FROM Basenews b JOIN Scrap s ON b.id = s.bnews.id " +
            "WHERE s.user.id = :userId AND b.scrap = true")
    Page<Basenews> findScrapNewsByUserId(@Param("userId") Long userId, Pageable pageable);
}