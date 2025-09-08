package com.study.odersystem.common.controller;

import com.study.odersystem.common.service.SseAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseAlarmController {
    private final SseAlarmService sseAlarmService;

    @GetMapping("")
    public SseEmitter connect(@RequestHeader("X-User-Email") String email) {
        return this.sseAlarmService.connect(email);
    }

    @DeleteMapping("")
    public void disconnect(@RequestHeader("X-User-Email") String email) {
        this.sseAlarmService.disconnect(email);
    }

}
