package com.jaimeg.json2api.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class ZipUtils {

    public void unZip(Path zipPath, Path outputDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path normalizedPath = outputDir.resolve(entry.getName()).normalize();
                if (!normalizedPath.startsWith(outputDir)) {
                    throw new IOException("Entrada ZIP inválida: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(normalizedPath);
                } else {
                    Files.createDirectories(normalizedPath.getParent());
                    try (OutputStream os = Files.newOutputStream(normalizedPath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public void zip(Path sourceDir, File outputZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {
            Files.walk(sourceDir).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry entry = new ZipEntry(sourceDir.relativize(path).toString());
                try {
                    zos.putNextEntry(entry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    public void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }

        boolean dirDeleted = directory.delete();
        if (!dirDeleted) {
            System.err.println("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

}
