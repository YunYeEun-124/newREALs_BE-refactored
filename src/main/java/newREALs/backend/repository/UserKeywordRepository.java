package newREALs.backend.repository;

import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword,Long> {
    @Query("SELECT n.keyword FROM UserKeyword n WHERE n.user.id = :userId")
    List<Keyword> findKeywordsById(Long userId);
}