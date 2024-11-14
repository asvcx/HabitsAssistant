package org.habitsapp.contract;

import org.habitsapp.model.AuditEvent;

import java.util.List;

public interface AuditRepo {
    void saveToLog(Long userId, String message);
    List<AuditEvent> getFromLog(Long userId, int limit, int offset);
}
