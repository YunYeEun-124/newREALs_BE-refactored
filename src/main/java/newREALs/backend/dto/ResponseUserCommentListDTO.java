package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class ResponseUserCommentListDTO {

    private List<UserCommentListDTO> insightList;
    private boolean hasNext;
    private boolean hasContent;
    private int nowPage;

}
