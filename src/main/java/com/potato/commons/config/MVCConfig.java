package com.potato.commons.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.potato.commons.interceptor.AuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableWebMvc
public class MVCConfig extends WebMvcConfigurerAdapter {

    public MVCConfig() {
    }

    /**
     * 注册服务
     *
     * @return
     */
    protected ServletRegistrationBean servletRegistrationBean() {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.scan(new String[]{"com.potato.controller"});
        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(dispatcherServlet, new String[0]);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings(new String[]{"/services/*", "/web/*"});
        registrationBean.setName("rest");
        return registrationBean;
    }


    /**
     * 添加拦截器
     * 多个拦截器组成一个拦截器链
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor());
        super.addInterceptors(registry);
    }

    /**
     * 添加返回结果处理器
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    /**
     * 重启服务处理
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean restServlet() {
        return this.servletRegistrationBean();
    }

    /**
     * 对返回的结果集进行处理
     *
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        formatDate(objectMapper);
        formatMediaType(mappingJackson2HttpMessageConverter);
        longToString(objectMapper);
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        return mappingJackson2HttpMessageConverter;
    }


    /**
     * 日期格式转换
     *
     * @param objectMapper
     */
    private void formatDate(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * 设置中文编码格式
     *
     * @param mappingJackson2HttpMessageConverter
     */
    private void formatMediaType(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        List<MediaType> list = new ArrayList<MediaType>();
        list.add(MediaType.APPLICATION_JSON_UTF8);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
    }

    /**
     * Long类型转String类型
     *
     * @param objectMapper
     */
    private void longToString(ObjectMapper objectMapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
    }
}
