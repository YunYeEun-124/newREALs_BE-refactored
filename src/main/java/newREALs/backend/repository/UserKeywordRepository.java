package newREALs.backend.repository;

import jakarta.transaction.Transactional;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword,Long> {
    @Query("SELECT n.keyword FROM UserKeyword n WHERE n.user.id = :userId")
    List<UserKeyword> findKeywordsByUserId(Long userId);
}