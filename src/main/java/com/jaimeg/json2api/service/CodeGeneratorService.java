package com.jaimeg.json2api.service;

import com.jaimeg.json2api.generator.ModelClassGenerator;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.JsonTransformer;
import com.jaimeg.json2api.util.JavaFileAdder;
import com.jaimeg.json2api.util.ZipUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CodeGeneratorService {

    private final ModelClassGenerator modelClassGenerator;
    private final ZipUtils zipUtils;
    private final JavaFileAdder javaFileAdder;

    public CodeGeneratorService(ModelClassGenerator modelClassGenerator, ZipUtils zipUtils, JavaFileAdder javaFileAdder) {
        this.modelClassGenerator = modelClassGenerator;
        this.zipUtils = zipUtils;
        this.javaFileAdder = javaFileAdder;
    }

    public byte[] generateCodeService(JsonTransformer jsonTransformer, byte[] zipBytes) {

        try {

            String basePackage = jsonTransformer.getGroup() + "." + jsonTransformer.getArtifact();
            String baseFolder = basePackage.replace(".", "/");
            baseFolder = baseFolder + "/model";
            Path tempDir = Files.createTempDirectory("spring-project-" + UUID.randomUUID());
            Path zipPath = tempDir.resolve("project.zip");
            Files.write(zipPath, zipBytes);

            zipUtils.unZip(zipPath, tempDir);


            Path srcPath = tempDir.resolve(jsonTransformer.getArtifact()).resolve("src/main/java").resolve(baseFolder);
            for (EntityStructure model : jsonTransformer.getModels()) {
                String code = modelClassGenerator.generateModelClassCode(model, jsonTransformer.getGroup(), jsonTransformer.getArtifact());
                javaFileAdder.addNewJavaFile(srcPath, code, model.getName(), "");
            }

            File finalZip = Files.createTempFile("final-project-", ".zip").toFile();
            zipUtils.zip(tempDir.resolve(jsonTransformer.getArtifact()), finalZip);


            byte[] resultBytes = Files.readAllBytes(finalZip.toPath());

            zipUtils.deleteDirectory(tempDir.toFile());

            return resultBytes;

            //TODO, HACER PETICION PARA RECOGER SPRING INITZLR,
            //TODO VER DEPENDENCIAS QUE NECESITO OBLIGATORIAMENTE EN EL POM
            //TODO AÑADIR DEMOMENTO LOS FICHEROS .JAVA A ESE ZIP
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
