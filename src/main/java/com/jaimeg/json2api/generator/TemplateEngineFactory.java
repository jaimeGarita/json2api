//package com.jaimeg.json2api.generator;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
//
//@Component
//public class TemplateEngineFactory {
//
//
//    public TemplateEngine createTextTemplateEngine() {
//        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
//        resolver.setPrefix("templates/");
//        resolver.setSuffix(".java");
//        resolver.setTemplateMode("TEXT");
//        resolver.setCharacterEncoding("UTF-8");
//        System.out.println("Plantillas Thymeleaf están en: " + resolver.getPrefix());
//
//        TemplateEngine templateEngine = new TemplateEngine();
//        templateEngine.setTemplateResolver(resolver);
//        return  templateEngine;
//    }
//
//}
