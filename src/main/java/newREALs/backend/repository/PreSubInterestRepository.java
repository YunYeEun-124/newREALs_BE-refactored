package newREALs.backend.repository;

import newREALs.backend.domain.PreSubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreSubInterestRepository extends JpaRepository<PreSubInterest, Long> {

}
