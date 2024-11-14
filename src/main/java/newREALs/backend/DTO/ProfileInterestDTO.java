package newREALs.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Builder
@AllArgsConstructor
public class ProfileInterestDTO {
    private String category;
    private String subCategory;
    private int percentage;
}