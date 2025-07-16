package com.jaimeg.json2api.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jaimeg.json2api.enums.ComponentType;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.util.strategy.ComponentStrategy;
import com.jaimeg.json2api.util.strategy.ControllerClassGenerator;
import com.jaimeg.json2api.util.strategy.ModelClassGenerator;
import com.jaimeg.json2api.util.strategy.ServiceClassGenerator;

@Component
public class GeneratorContext {
    private final Map<ComponentType, ComponentStrategy> strategies;

    public GeneratorContext(
        ModelClassGenerator modelGenerator,
        ControllerClassGenerator controllerClassGenerator,
        ServiceClassGenerator serviceClassStrategy) {

        strategies = new HashMap<>();
        strategies.put(ComponentType.TABLE, modelGenerator);
        strategies.put(ComponentType.CONTROLLER, controllerClassGenerator);
        strategies.put(ComponentType.SERVICE, serviceClassStrategy);
    }

    public String generateCode(ComponentType type, EntityStructure entity, String group, String artifact){
        ComponentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException();
        }
        return strategy.generate(entity, group, artifact);
    }

}
