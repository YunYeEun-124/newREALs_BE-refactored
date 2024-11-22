package newREALs.backend.dto;

import lombok.Getter;

@Getter
public class SimpleNewsDto {
    private Long basenewsID;
    private String title;

    public SimpleNewsDto(Long basenewsID, String title) {
        this.basenewsID = basenewsID;
        this.title = title;
    }
}
