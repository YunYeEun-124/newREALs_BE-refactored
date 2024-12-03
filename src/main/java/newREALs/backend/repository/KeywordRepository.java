package newREALs.backend.repository;

import newREALs.backend.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface KeywordRepository extends JpaRepository<Keyword,Long> {

    @Query("SELECT k.name FROM Keyword k WHERE k.id = :id")
    String findNameById(@Param("id") Long id);

    Keyword getByName(String keywordName);
    Optional<Keyword> findByName(String key);
    @Query("SELECT k.name FROM Keyword k JOIN k.category c WHERE c.name = :categoryname")
    List<String> findAllByCategory_Name(@Param("categoryname") String categoryname);

    @Query("SELECT k FROM Keyword k JOIN k.category c WHERE c.name = :categoryname")
    List<Keyword> findAllByCategoryName(@Param("categoryname") String categoryname);
}