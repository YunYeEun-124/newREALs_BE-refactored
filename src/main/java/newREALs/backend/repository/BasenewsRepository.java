package newREALs.backend.repository;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Category;
import newREALs.backend.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasenewsRepository extends JpaRepository<Basenews,Long> {
    List<Basenews> findByIsDailyNewsTrue();

   
    //List<Basenews> findByCategoryAndSubCategoryOrderByIdAsc(String category, String subCategory);
    List<Basenews> findByCategoryAndSubCategoryOrderByIdAsc(Category category, SubCategory subCategory);

    List<Basenews> findByCategoryOrderByIdAsc(Category category);
}
