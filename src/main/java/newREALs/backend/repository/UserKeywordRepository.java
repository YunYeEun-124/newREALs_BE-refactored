package newREALs.backend.repository;

import jakarta.transaction.Transactional;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword,Long> {
    @Query("SELECT n.keyword FROM UserKeyword n WHERE n.user.id = :userId")
    List<Keyword> findKeywordsById(Long userId);


    List<UserKeyword> findAllByUserId(Long userid);


    @Query("SELECT k.keyword.name FROM UserKeyword k JOIN k.user u WHERE u.id = :userId")
    List<String> findAllByUser_Id(@Param("userId") Long userId);

    Optional<UserKeyword> findByUser_IdAndKeyword_Name(Long userId, String keywordName);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserKeyword uk WHERE uk.user.id = :userId AND uk.keyword.name = :key")
    void deleteByUser_IdAndKeyword_Name(@Param("userId") Long userId, @Param("key") String key);


    boolean existsByUserId(Long userId);
}