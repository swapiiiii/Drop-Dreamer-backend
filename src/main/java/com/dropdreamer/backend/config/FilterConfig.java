package com.dropdreamer.backend.config;

import com.dropdreamer.backend.filter.JwtFilter;
import com.dropdreamer.backend.util.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    // ✅ Create JwtFilter bean manually
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }

    // ✅ Register filter for /auth/* URLs

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/products/*", "/admin/*"); // ✅ protect these routes
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
