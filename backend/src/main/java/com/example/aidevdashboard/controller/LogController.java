package com.example.aidevdashboard.controller;

import com.example.aidevdashboard.dto.LogRequest;
import com.example.aidevdashboard.dto.LogResponse;
import com.example.aidevdashboard.service.ErrorLogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final ErrorLogService errorLogService;

    public LogController(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @GetMapping
    public List<LogResponse> findAll() {
        return errorLogService.findAll();
    }

    @GetMapping("/unresolved")
    public List<LogResponse> findUnresolved() {
        return errorLogService.findUnresolved();
    }

    @GetMapping("/{id}")
    public LogResponse findById(@PathVariable Long id) {
        return errorLogService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LogResponse create(@Valid @RequestBody LogRequest request) {
        return errorLogService.create(request);
    }

    @PutMapping("/{id}")
    public LogResponse update(@PathVariable Long id, @Valid @RequestBody LogRequest request) {
        return errorLogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        errorLogService.delete(id);
    }
}
