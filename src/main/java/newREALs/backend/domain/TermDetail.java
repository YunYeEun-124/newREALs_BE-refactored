package newREALs.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class TermDetail{

    @Column
    private String term;

    @Column
    private String termInfo;

    public TermDetail(String term, String info){
        this.term = term;
        this.termInfo = info;
    }
}
