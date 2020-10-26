package com.rengu.operationsmanagementsuitev3.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:19
 **/

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey("rengu");
        return jwtAccessTokenConverter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors();
        // 放行所有Option请求
        http.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll();
        // 放行用户注册接口
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/users/user").permitAll();
        // 放行swagger2文档页面
        http.authorizeRequests().antMatchers("/swagger-ui.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll();
        // 放行文件导出接口
        http.authorizeRequests().antMatchers("/components/*/export", "/componentfiles/*/export", "/componenthistorys/*/export", "/componentfilehistorys/*/export").permitAll();
        // 放行websocket接口
        http.authorizeRequests().antMatchers("/OMS/**").permitAll();
        // 放行actuator接口
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
    }
}
