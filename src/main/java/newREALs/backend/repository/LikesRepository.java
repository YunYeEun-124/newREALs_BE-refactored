package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LikesRepository extends JpaRepository<Likes,Long> {

    Optional<Likes> findByUserAndBasenews(Accounts user, Basenews basenews);
}
