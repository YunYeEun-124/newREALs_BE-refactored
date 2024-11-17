package newREALs.backend.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GptRequestDto {
    private String model;
    private List<Map<String,String>> messages;
    private float temperature;
    private int max_tokens;
}
