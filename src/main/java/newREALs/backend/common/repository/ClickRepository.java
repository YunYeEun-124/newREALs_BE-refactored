package newREALs.backend.common.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.Basenews;
import newREALs.backend.accounts.domain.UserNewsClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ClickRepository extends JpaRepository<UserNewsClick,Long> {
    Optional<UserNewsClick> findByUserAndBasenews(Accounts user, Basenews basenews);
}
