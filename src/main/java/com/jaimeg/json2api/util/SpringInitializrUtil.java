package com.jaimeg.json2api.util;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

@Component
public class SpringInitializrUtil {

    private static final String SPRING_INITIALIZR_API = "https://start.spring.io/starter.zip";

    public byte[] generateProjectFromInitializr(String group, String artifact, String packageName, String dependencies, String description, String javaVersion, String packaging) {
        byte[] zipBytes = null;
        try {
            String url = buildSpringInitializrUrl(group, artifact, description, packaging, javaVersion, packageName, dependencies);
            zipBytes = getZipFromInitializr(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zipBytes;
    }

    private String buildSpringInitializrUrl(String group, String artifact, String description, String packaging, String javaVersion, String packageName, String dependencies) {
        return UriComponentsBuilder.fromHttpUrl(SPRING_INITIALIZR_API)
                .queryParam("type", "maven-project")
                .queryParam("language", "java")
                .queryParam("bootVersion", "3.4.4")
                .queryParam("baseDir", artifact)
                .queryParam("groupId", group)
                .queryParam("artifactId", artifact)
                .queryParam("name", artifact)
                .queryParam("description", description)
                .queryParam("packageName", packageName)
                .queryParam("packaging", packaging)
                .queryParam("javaVersion", javaVersion)
                .queryParam("dependencies", dependencies)
                .toUriString();
    }

    private byte[] getZipFromInitializr(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                null,
                byte[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Error al generar el proyecto desde Spring Initializr, Código de respuesta: " + response.getStatusCode());
        }
    }


}
