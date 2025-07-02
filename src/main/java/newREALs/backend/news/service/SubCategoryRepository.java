package newREALs.backend.news.service;

import newREALs.backend.news.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {
     Long findSubCategoryIdByName(String subCategory);

     Optional<SubCategory> findByName(String subCate);
}