package com.jaimeg.json2api.service;

import com.jaimeg.json2api.context.GeneratorContext;
import com.jaimeg.json2api.enums.ComponentType;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.JsonTransformer;
import com.jaimeg.json2api.util.JavaFileAdder;
import com.jaimeg.json2api.util.ZipUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class CodeGeneratorService {

    private final GeneratorContext generatorContext;
    private final ZipUtils zipUtils;
    private final JavaFileAdder javaFileAdder;

    public CodeGeneratorService(GeneratorContext generatorContext, ZipUtils zipUtils, JavaFileAdder javaFileAdder) {
        this.generatorContext = generatorContext;
        this.zipUtils = zipUtils;
        this.javaFileAdder = javaFileAdder;
    }

    public byte[] generateCodeService(JsonTransformer jsonTransformer, byte[] zipBytes) {

        try {
            String basePackage = jsonTransformer.getGroup() + "." + jsonTransformer.getArtifact();
            String baseFolder = basePackage.replace(".", "/");

            Path tempDir = Files.createTempDirectory("spring-project-" + UUID.randomUUID());
            Path zipPath = tempDir.resolve("project.zip");
            Files.write(zipPath, zipBytes);

            zipUtils.unZip(zipPath, tempDir);
            Path srcPath = tempDir.resolve(jsonTransformer.getArtifact())
                    .resolve("src/main/java")
                    .resolve(baseFolder);

            for (EntityStructure model : jsonTransformer.getModels()) {
                String modelCode = generatorContext.generateCode(
                        ComponentType.TABLE, model,
                        jsonTransformer.getGroup(),
                        jsonTransformer.getArtifact());
                Path modelPath = srcPath.resolve("model");
                javaFileAdder.addNewJavaFile(modelPath, modelCode, model.getName(), "");

                for (ComponentType type : jsonTransformer.getGenerationOptions().getEnabledComponentType()) {

                    if (type == ComponentType.TABLE)
                        continue;

                    String code = generatorContext.generateCode(
                            type, model,
                            jsonTransformer.getGroup(),
                            jsonTransformer.getArtifact());

                    String suffix = type.name().charAt(0) + type.name().substring(1).toLowerCase();

                    String packageFolder = switch (type) {
                        case CONTROLLER -> "controller";
                        case SERVICE -> "service";
                        case REPOSITORY -> "repository";
                        default -> "model";
                    };

                    Path componentPath = srcPath.resolve(packageFolder);
                    javaFileAdder.addNewJavaFile(componentPath, code, model.getName() + suffix, "");
                }
            }

            File finalZip = Files.createTempFile("final-project-", ".zip").toFile();
            zipUtils.zip(tempDir.resolve(jsonTransformer.getArtifact()), finalZip);

            byte[] resultBytes = Files.readAllBytes(finalZip.toPath());

            zipUtils.deleteDirectory(tempDir.toFile());

            return resultBytes;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
