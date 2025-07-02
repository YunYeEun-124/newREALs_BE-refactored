package newREALs.backend.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class userKeywordDTO {
    // private Long userId;
    private String keywordName;
    private Long keywordId;
    // private String subCategoryName;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserCommentListDTO {

        private String topic;
        private String userComment;
        private Long newsId;
    }
}