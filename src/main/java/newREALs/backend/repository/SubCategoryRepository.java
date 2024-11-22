package newREALs.backend.repository;

import newREALs.backend.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {
     Long findSubCategoryIdByName(String subCategory);

     Optional<SubCategory> findByName(String subCate);
}