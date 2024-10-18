package searchenginepackage.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainPageController {
    @RequestMapping("/")
    public String mainPage(Model model) {
        return "index";
    }
}
