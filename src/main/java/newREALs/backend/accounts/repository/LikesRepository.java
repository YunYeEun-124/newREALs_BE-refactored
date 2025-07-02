package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.accounts.domain.Likes;
import newREALs.backend.news.domain.Basenews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes,Long> {

    Optional<Likes> findByUserAndBasenews(Accounts user, Basenews basenews);
}