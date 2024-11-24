package newREALs.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.ThinkComment;
import newREALs.backend.dto.InsightDTO;
import newREALs.backend.repository.InsightRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsightService {

    private  final InsightRepository insightRepository;


    public List<InsightDTO> getInsight(){

        List<InsightDTO> result = insightRepository.findAllBy();


        if(result.size() != 5){
            System.out.println("sth wrong. insight size is not 5");
            return null;
        }

        return result;
    }
}
