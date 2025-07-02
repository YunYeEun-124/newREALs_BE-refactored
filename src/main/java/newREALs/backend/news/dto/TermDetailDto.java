package newREALs.backend.news.dto;

import lombok.Getter;
import newREALs.backend.news.domain.TermDetail;

@Getter
public class TermDetailDto {

    private String term;
    private String termInfo;

    public TermDetailDto(TermDetail termDetail) {
        this.term = termDetail.getTerm();
        this.termInfo = termDetail.getTermInfo();
    }

    public String getTerm() {
        return term;
    }

    public String getTermInfo() {
        return termInfo;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setTermInfo(String termInfo) {
        this.termInfo = termInfo;
    }
}
