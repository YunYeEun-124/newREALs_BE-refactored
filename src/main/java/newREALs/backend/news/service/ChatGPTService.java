package newREALs.backend.news.service;

import java.util.List;
import java.util.Map;

public interface ChatGPTService {
    Map<String,Object> generateContent(List<Map<String,String>> messages);
}
