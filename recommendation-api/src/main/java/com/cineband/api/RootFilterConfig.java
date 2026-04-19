package com.cineband.api;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RootFilterConfig {

    @Bean
    public FilterRegistrationBean<RootPathFilter> rootPathFilterRegistration() {
        FilterRegistrationBean<RootPathFilter> reg = new FilterRegistrationBean<>(new RootPathFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.addUrlPatterns("/");
        reg.setName("cinebandRootPathFilter");
        return reg;
    }
}
