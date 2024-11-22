package newREALs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import newREALs.backend.config.ChatGPTConfig;
import newREALs.backend.dto.GptRequestDto;
import newREALs.backend.dto.GptResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatGPTService {
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final String model = "gpt-4-turbo";

    //요약, 설명, 용어 정리 생성하는 메서드
    @Override
    public Map<String, Object> generateContent(List<Map<String,String>> messages) {

        // GptRequestDto에 messages 필드 전달
        GptRequestDto requestDto = GptRequestDto.builder()
                .model(model)
                .messages(messages)
                .temperature(0.7f)
                .max_tokens(2000)
                .build();

        HttpEntity<GptRequestDto> entity = new HttpEntity<>(requestDto, headers);

        //엔드포인트
        String url = "https://api.openai.com/v1/chat/completions";
        //log.info("GPT Request: {}", requestDto); //요청 로그
        ResponseEntity<GptResponseDto> response = restTemplate.postForEntity(url, entity, GptResponseDto.class);
        //log.info("GPT Response: {}", response.getBody());//응답로그

        try {
            // ObjectMapper로 gpt 답변 파싱하기
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.convertValue(response.getBody(), new TypeReference<>() {});

            //choices에서 첫 번째 응답의 message를 가져옴
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String text = (String) message.get("content");

            return Map.of("text", text); // 반환값을 Map으로 래핑하여 반환
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response from GPT", e);
        }
    }


}
