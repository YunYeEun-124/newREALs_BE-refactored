package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProfileInfoDto {
    private Long user_id;
    private String name;
    private String email;
    private String profilePath;
    private int point;
    private List<String> keywords;
}
