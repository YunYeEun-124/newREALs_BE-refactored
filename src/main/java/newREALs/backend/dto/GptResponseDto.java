package newREALs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class GptResponseDto {
    @JsonProperty("choices")
    private List<Map<String,Object>> choices;
}
