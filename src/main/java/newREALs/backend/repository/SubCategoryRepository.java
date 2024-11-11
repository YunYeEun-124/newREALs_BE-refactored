package newREALs.backend.repository;


import newREALs.backend.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {
    Long findSubCategoryIdByName(String subCategory);


}