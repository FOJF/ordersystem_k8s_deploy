package com.study.odersystem.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathCheckController {
    @GetMapping("/health")
    public String healthCheck() {
        return "OK_k8s_abc\n";
    }
}
