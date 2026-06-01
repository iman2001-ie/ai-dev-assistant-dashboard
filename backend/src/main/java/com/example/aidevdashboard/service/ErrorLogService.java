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
    private final CurrentUserService currentUserService;

    public ErrorLogService(ErrorLogRepository errorLogRepository, CurrentUserService currentUserService) {
        this.errorLogRepository = errorLogRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findAll() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return errorLogRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(LogResponse::fromEntity).toList();
        }
        return errorLogRepository.findAll().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findRecent() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return errorLogRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream().map(LogResponse::fromEntity).toList();
        }
        return errorLogRepository.findTop5ByOrderByCreatedAtDesc().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<LogResponse> findUnresolved() {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return errorLogRepository.findByResolvedFalseAndUserIdOrderByCreatedAtDesc(userId).stream().map(LogResponse::fromEntity).toList();
        }
        return errorLogRepository.findByResolvedFalseOrderByCreatedAtDesc().stream().map(LogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public LogResponse findById(Long id) {
        return LogResponse.fromEntity(getEntity(id));
    }

    @Transactional(readOnly = true)
    public ErrorLog getEntity(Long id) {
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return errorLogRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied to log " + id));
        }
        return errorLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found with id " + id));
    }

    @Transactional
    public LogResponse create(LogRequest request) {
        ErrorLog log = new ErrorLog();
        applyRequest(log, request);
        Long userId = currentUserService.currentUserId();
        if (userId != null) log.setUserId(userId);
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
        Long userId = currentUserService.currentUserId();
        if (userId != null) {
            return errorLogRepository.countByResolvedFalseAndUserId(userId);
        }
        return errorLogRepository.countByResolvedFalse();
    }

    private void applyRequest(ErrorLog log, LogRequest request) {
        log.setTitle(request.title());
        log.setContent(request.content());
        log.setSource(request.source());
        log.setResolved(request.resolved());
    }
}
