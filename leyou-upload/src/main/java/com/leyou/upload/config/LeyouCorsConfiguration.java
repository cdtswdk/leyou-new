package com.leyou.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter(){

        //添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //是否发送cookie信息
        config.setAllowCredentials(true);
        //设置允许的域，不要写*，否则cookie就无法使用了
        config.addAllowedOrigin("http://manage.leyou.com");
        //允许的请求方式
        config.addAllowedMethod("*");
        //允许的请求头
        config.addAllowedHeader("*");

        //添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);

        return new CorsFilter(configurationSource);
    }
}
