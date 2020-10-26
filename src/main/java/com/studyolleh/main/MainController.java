package com.studyolleh.main;

import com.studyolleh.account.CurrentUser;
import com.studyolleh.account.LoginForm;
import com.studyolleh.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(new LoginForm());
        return "login";
    }
}
