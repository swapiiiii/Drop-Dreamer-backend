package com.dropdreamer.backend.config;

import com.dropdreamer.backend.filter.JwtFilter;
import com.dropdreamer.backend.util.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);

        // Protect cart, products & admin routes
        registrationBean.addUrlPatterns(
                "/cart/*",
                "/products/*",
                "/products/**",
                "/admin/*",
                "/admin/**"
        );

        registrationBean.setOrder(1);
        return registrationBean;
    }
}
