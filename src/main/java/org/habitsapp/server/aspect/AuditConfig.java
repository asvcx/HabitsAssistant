package org.habitsapp.server.aspect;

import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {

    @Bean
    public AuditUser getAutowireAuditUser() {
        return Aspects.aspectOf(AuditUser.class);
    }

    @Bean
    public AuditHabit getAutowireAuditHabit() {
        return Aspects.aspectOf(AuditHabit.class);
    }

}
