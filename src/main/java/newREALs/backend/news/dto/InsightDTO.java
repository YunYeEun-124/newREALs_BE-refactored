package newREALs.backend.news.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsightDTO {

    private String topic;
    private String category;
    private Long basenewsId;


}
