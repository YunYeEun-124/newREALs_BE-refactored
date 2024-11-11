package newREALs.backend.repository;

import newREALs.backend.domain.Dailynews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyNewsRepository extends JpaRepository<Dailynews,Long> {
    
}