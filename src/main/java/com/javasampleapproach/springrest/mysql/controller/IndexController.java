package com.javasampleapproach.springrest.mysql.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
 
@Controller
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "uploadfile";
    }
}