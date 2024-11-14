package org.habitsapp;

import org.habitsapp.aspect.AuditHabitAspect;
import org.habitsapp.aspect.AuditUserAspect;
import org.habitsapp.aspect.LoggingAspect;
import org.habitsapp.contract.AuditRepo;
import org.habitsapp.contract.HabitService;
import org.habitsapp.contract.UserService;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
@DependsOn("auditRepoImpl")
@EnableAspectJAutoProxy
public class AuditConfig {

    @Autowired
    UserService userService;
    @Autowired
    HabitService habitService;
    @Autowired
    AuditRepo auditRepo;

    @Bean
    @Primary
    public UserService getUserService() {
        AspectJProxyFactory factory = new AspectJProxyFactory(userService);
        AuditUserAspect aspect = new AuditUserAspect();
        aspect.setAuditRepo(auditRepo);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @Bean
    @Primary
    public HabitService getHabitService() {
        AspectJProxyFactory factory = new AspectJProxyFactory(habitService);
        AuditHabitAspect aspect = new AuditHabitAspect();
        aspect.setAuditRepo(auditRepo);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @Bean
    @Primary
    public LoggingAspect getLoggingAspect() {
        return new LoggingAspect();
    }

}
