package newREALs.backend.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import newREALs.backend.accounts.domain.Accounts;

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

    public static ProfileInfoDto from(Accounts user, List<String> keywords) {
        return new ProfileInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePath(),
                user.getPoint(),
                keywords
        );
    }
}
