package newREALs.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCommentListDTO {

    private String topic;
    private String userComment;
    private Long newsId;
}
