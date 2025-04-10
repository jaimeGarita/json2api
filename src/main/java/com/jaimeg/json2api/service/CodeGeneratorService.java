package com.jaimeg.json2api.service;

import com.jaimeg.json2api.generator.ModelClassGenerator;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.JsonTransformer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeGeneratorService {

    private final ModelClassGenerator modelClassGenerator;

    public CodeGeneratorService(ModelClassGenerator modelClassGenerator) {
        this.modelClassGenerator = modelClassGenerator;
    }

    public void generateCodeService(JsonTransformer jsonTransformer) {
        List<byte[]> contentFiles = new ArrayList<>();
        jsonTransformer.getModels().forEach((model) -> {
            String code = modelClassGenerator.generateModelClassCode(model, jsonTransformer.getGroup(), jsonTransformer.getArtifact());
            System.out.println(code);
            byte[] fileContent = code.getBytes(StandardCharsets.UTF_8);
            contentFiles.add(fileContent);
        });

        //TODO, HACER PETICION PARA RECOGER SPRING INITZLR,
        //TODO VER DEPENDENCIAS QUE NECESITO OBLIGATORIAMENTE EN EL POM
        //TODO AÑADIR DEMOMENTO LOS FICHEROS .JAVA A ESE ZIP

    }

}
