package newREALs.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts")
public class AccountsController {
    @GetMapping
    public String loginWithKakao() {
        return "redirect:/oauth2/authorization/kakao";
    }
}
