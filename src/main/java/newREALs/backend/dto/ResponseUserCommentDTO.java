package newREALs.backend.dto;

import lombok.Getter;

@Getter
public class ResponseUserCommentDTO {
    private String topic;
    private String userComment;

    //response 1. 사용자 댓글 안 모아짐
    private String AIComment;


    //response 2. 사용자 댓글 모아짐.
    private String pros;
    private String cons;
    private String neutral;

    public ResponseUserCommentDTO(String topic ){
        this.topic = topic;
    }

    public ResponseUserCommentDTO(String topic, String userComment, String AIComment){
        this.AIComment = AIComment;
        this.topic= topic;
        this.userComment = userComment;
    }

    public ResponseUserCommentDTO(String topic, String userComment, String pros, String cons, String neutral){
        this.pros = pros;
        this.cons = cons;
        this.neutral = neutral;
        this.topic= topic;
        this.userComment = userComment;
    }




}
