package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.domain.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubInterestRepository extends JpaRepository<SubInterest,Long> {

    Optional<SubInterest> findByUserAndSubCategory(Accounts user, SubCategory subCategory);
}
