package newREALs.backend.news.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class TermDetail{

    @Column(name="term")
    private String term;

    @Column(name="term_info",length=1000)
    private String termInfo;

    public TermDetail(String term, String info){
        this.term = term;
        this.termInfo = info;
    }
}
