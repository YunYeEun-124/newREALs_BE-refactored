package newREALs.backend.repository;

import newREALs.backend.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {


     Optional<SubCategory> findByName(String subCate);
}
