package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ScrapRepository extends JpaRepository<Scrap,Long> {
    Optional<Scrap> findByUserAndBasenews(Accounts user, Basenews basenews);
    //Scrap 엔티티 필드 이름이 user, bnews이므로 메서드 이름도 맞춰야 함..

    Boolean existsByUser_IdAndBasenews_Id(Long userId, Long basenewsId);
}
