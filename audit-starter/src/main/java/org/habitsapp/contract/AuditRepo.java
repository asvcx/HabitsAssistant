package org.habitsapp.contract;

import org.habitsapp.model.AuditEvent;

import java.util.List;

public interface AuditRepo {
    abstract void saveToLog(Long userId, String message);
    abstract List<AuditEvent> getFromLog(Long userId, int limit, int offset);
}
