package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
@AllArgsConstructor
public class ProfileInterestDTO {
    private String category;
    private String subCategory;
    private int percentage;
}