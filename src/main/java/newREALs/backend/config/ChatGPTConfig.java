package newREALs.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatGPTConfig {
    @Value("${openai.secret-key}")
    private String secretKey;

    @Value("${openai.organization-id}")
    private String organizationId;

    //RestTemplate 사용하기 위한 객체 만들기
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

    // HttpHeader에서 JWT 토큰으로 Bearer 토큰 값을 입력해서 전송하기 위한 공통 Header 만들기
    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.set("OpenAI-Organization", organizationId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
