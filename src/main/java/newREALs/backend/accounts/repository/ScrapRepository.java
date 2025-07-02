package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.Basenews;
import newREALs.backend.accounts.domain.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ScrapRepository extends JpaRepository<Scrap,Long> {
    Optional<Scrap> findByUserAndBasenews(Accounts user, Basenews basenews);
    //Scrap 엔티티 필드 이름이 user, bnews이므로 메서드 이름도 맞춰야 함..

    Boolean existsByUser_IdAndBasenews_Id(Long userId, Long basenewsId);

    Page<Scrap> findByUser(Accounts user, Pageable pageable);

    @Query("SELECT s.basenews FROM Scrap s " + "WHERE s.user.id = :userId " +
            "AND ((s.basenews.title LIKE %:keyword%) OR (s.basenews.description LIKE %:keyword%))")
    Page<Basenews> findByUserAndTitleContainingOrDescriptionContaining(Long userId, String keyword, Pageable pageable);

}
