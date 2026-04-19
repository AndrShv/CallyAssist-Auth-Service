package com.example.project.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class NgrokConfig {

    @Bean
    public FilterRegistrationBean<Filter> ngrokFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter((request, response, chain) -> {
            ((HttpServletResponse) response)
                    .setHeader("ngrok-skip-browser-warning", "true");
            chain.doFilter(request, response);
        });
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
