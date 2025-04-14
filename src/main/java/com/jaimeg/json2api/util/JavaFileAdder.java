package com.jaimeg.json2api.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JavaFileAdder {

    public void addNewJavaFile(Path projectDir, String code, String className, String folderName) throws IOException {
        //"src/main/java/com/example/newfolder"
        Path newFolderPath = projectDir.resolve(folderName);
        Files.createDirectories(newFolderPath);

        Path newFilePath = newFolderPath.resolve(className + ".java");
        Files.write(newFilePath, code.getBytes());
    }
}
