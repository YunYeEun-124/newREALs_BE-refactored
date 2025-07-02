package newREALs.backend.accounts.dto;

import lombok.Getter;
import lombok.Setter;
import newREALs.backend.news.domain.Basenews;

@Getter
@Setter
public class LikesDTO {
    private int good;
    private int interesting;
    private int bad;
    private int reactionType;

    public LikesDTO(Basenews basenews,int reactionType){
        this.good=basenews.getLikesCounts()[0];
        this.bad=basenews.getLikesCounts()[2];
        this.interesting=basenews.getLikesCounts()[1];
        this.reactionType=reactionType;
    }

}
