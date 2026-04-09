package com.group91.tars.web;

import com.group91.tars.service.TarsService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TarsService.getInstance().initializeStorage();
    }
}
