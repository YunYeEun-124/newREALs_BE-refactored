package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.domain.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SubInterestRepository extends JpaRepository<SubInterest,Long> {

    Optional<SubInterest> findByUserAndSubCategory(Accounts user, SubCategory subCategory);
    Optional<SubInterest> findByUserAndSubCategoryId(Accounts user, Long subCategoryId);
}
