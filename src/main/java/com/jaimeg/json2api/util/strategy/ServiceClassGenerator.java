package com.jaimeg.json2api.util.strategy;

import org.springframework.stereotype.Component;

import com.jaimeg.json2api.models.EntityStructure;

@Component
public class ServiceClassGenerator implements ComponentStrategy{
    
    @Override
    public String generate(EntityStructure entityStructure, String group, String artifact) {
        return "";
    }
    
}
