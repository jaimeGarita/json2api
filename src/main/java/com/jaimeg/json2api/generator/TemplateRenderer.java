//package com.jaimeg.json2api.generator;
//
//import com.jaimeg.json2api.models.ModelStruct;
//import org.springframework.stereotype.Component;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import java.util.Map;
//
//@Component
//public class TemplateRenderer {
//
//    private final TemplateEngine templateEngine;
//    private final TemplateEngineFactory templateEngineFactory;
//
//    public TemplateRenderer(TemplateEngineFactory templateEngineFactory , TemplateEngine templateEngine){
//        this.templateEngineFactory = templateEngineFactory;
//        this.templateEngine = templateEngineFactory.createTextTemplateEngine();
//    }
//
//    public String render(String templateName, Map<String, Object> variables) {
//        Context context = new Context();
//        context.setVariables(variables);
//        return this.templateEngine.process(templateName, context);
//    }
//}
