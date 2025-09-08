package com.study.odersystem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathCheckController {
    @GetMapping("/health")
    public String healthCheck() {
        return "OK_msa_member\n";
    }
}
