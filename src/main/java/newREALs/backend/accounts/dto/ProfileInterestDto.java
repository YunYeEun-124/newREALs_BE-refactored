package newREALs.backend.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
@AllArgsConstructor
public class ProfileInterestDto {
    private String category;
    private String subCategory;
    private int percentage;
}