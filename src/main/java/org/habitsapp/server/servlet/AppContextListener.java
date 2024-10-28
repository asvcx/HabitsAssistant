package org.habitsapp.server.servlet;

import org.habitsapp.annotations.Measurable;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.server.repository.DataLoader;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@Measurable
@WebListener
public class AppContextListener implements ServletContextListener {

    private ApplicationContext applicationContext;
    private DataLoader dataLoader;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        applicationContext = ApplicationContext.getInstance();
        dataLoader = new DataLoader(applicationContext);
        dataLoader.load();
        sce.getServletContext().setAttribute("appContext", applicationContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        dataLoader.release();
    }

}
