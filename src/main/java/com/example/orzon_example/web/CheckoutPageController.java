package com.example.orzon_example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutPageController {

    @GetMapping("/checkout")
    public String view() {
        return "checkout";
    }
}
