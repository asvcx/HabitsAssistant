package org.habitsapp.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDto {
    private Long userId;
    private String message;
    private Instant time;
}
