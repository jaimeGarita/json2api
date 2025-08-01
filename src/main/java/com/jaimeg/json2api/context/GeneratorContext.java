package com.jaimeg.json2api.context;

import java.util.EnumMap;
import org.springframework.stereotype.Component;

import com.jaimeg.json2api.enums.ComponentType;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.util.strategy.ComponentStrategy;
import com.jaimeg.json2api.util.strategy.ControllerClassGenerator;
import com.jaimeg.json2api.util.strategy.ModelClassGenerator;
import com.jaimeg.json2api.util.strategy.RepositoryClassGenerator;
import com.jaimeg.json2api.util.strategy.ServiceClassGenerator;

@Component
public class GeneratorContext {
    private final EnumMap<ComponentType, ComponentStrategy> strategies;

    public GeneratorContext(
        ModelClassGenerator modelGenerator,
        ControllerClassGenerator controllerClassGenerator,
        ServiceClassGenerator serviceClassGenerator,
        RepositoryClassGenerator repositoryClassGenerator
        ) {

        strategies = new EnumMap<>(ComponentType.class);
        strategies.put(ComponentType.TABLE, modelGenerator);
        strategies.put(ComponentType.CONTROLLER, controllerClassGenerator);
        strategies.put(ComponentType.SERVICE, serviceClassGenerator);
        strategies.put(ComponentType.REPOSITORY, repositoryClassGenerator);
    }

    public String generateCode(ComponentType type, EntityStructure entity, String group, String artifact){
        ComponentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException();
        }
        return strategy.generate(entity, group, artifact);
    }

}
