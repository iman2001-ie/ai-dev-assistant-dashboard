package com.example.aidevdashboard.service;

import com.example.aidevdashboard.dto.LogRequest;
import com.example.aidevdashboard.dto.LogResponse;
import com.example.aidevdashboard.model.ErrorLog;
import com.example.aidevdashboard.repository.ErrorLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ErrorLogService {
    private final ErrorLogRepository errorLogRepository;

    public ErrorLogService(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findAll() {
        return errorLogRepository.findAll().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findRecent() {
        return errorLogRepository.findTop5ByOrderByCreatedAtDesc().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findUnresolved() {
        return errorLogRepository.findByResolvedFalseOrderByCreatedAtDesc().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public LogResponse findById(Long id) {
        return LogResponse.fromEntity(getEntity(id));
    }

    @Transactional(readOnly = true)
    public ErrorLog getEntity(Long id) {
        return errorLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found with id " + id));
    }

    @Transactional
    public LogResponse create(LogRequest request) {
        ErrorLog log = new ErrorLog();
        applyRequest(log, request);
        return LogResponse.fromEntity(errorLogRepository.save(log));
    }

    @Transactional
    public LogResponse update(Long id, LogRequest request) {
        ErrorLog log = getEntity(id);
        applyRequest(log, request);
        return LogResponse.fromEntity(errorLogRepository.save(log));
    }

    @Transactional
    public void delete(Long id) {
        ErrorLog log = getEntity(id);
        errorLogRepository.delete(log);
    }

    @Transactional(readOnly = true)
    public long countUnresolved() {
        return errorLogRepository.countByResolvedFalse();
    }

    private void applyRequest(ErrorLog log, LogRequest request) {
        log.setTitle(request.title());
        log.setContent(request.content());
        log.setSource(request.source());
        log.setResolved(request.resolved());
    }
}
