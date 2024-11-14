package newREALs.backend.controller;

import newREALs.backend.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class ChatGPTController {

    private final NewsService newsService;

    public ChatGPTController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping("/process/{id}")
    public ResponseEntity<String> processNews(@PathVariable Long id) throws Throwable {
        newsService.processArticle(id);
        return ResponseEntity.ok("News processing completed for ID: " + id);
    }

}
