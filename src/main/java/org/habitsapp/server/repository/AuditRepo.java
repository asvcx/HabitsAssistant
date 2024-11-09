package org.habitsapp.server.repository;

import org.habitsapp.exchange.AuditEventDto;

import java.util.List;

public interface AuditRepo {
    void saveToLog(Long userId, String message);
    List<AuditEventDto> getFromLog(Long userId, int limit, int offset);
}
