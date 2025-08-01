package com.jaimeg.json2api.util;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class SpringInitializrUtil {

    private static final String SPRING_INITIALIZR_API = "https://start.spring.io/starter.zip";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_BOOT_VERSION = "bootVersion";
    private static final String PARAM_BASE_DIR = "baseDir";
    private static final String PARAM_GROUP_ID = "groupId";
    private static final String PARAM_ARTIFACT_ID = "artifactId";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String PARAM_PACKAGING = "packaging";
    private static final String PARAM_JAVA_VERSION = "javaVersion";
    private static final String PARAM_DEPENDENCIES = "dependencies";

    public byte[] generateProjectFromInitializr(String group, String artifact, String packageName, String dependencies,
            String description, String javaVersion, String packaging) {
        byte[] zipBytes = null;
        try {
            if (dependencies != null) {
                dependencies += ",web,lombok,data-jpa";
            } else {
                dependencies = "web,lombok,data-jpa";
            }
            String url = buildSpringInitializrUrl(group, artifact, description, packaging, javaVersion, packageName,
                    dependencies);
            zipBytes = getZipFromInitializr(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zipBytes;
    }

    private String buildSpringInitializrUrl(String group, String artifact, String description, String packaging,
            String javaVersion, String packageName, String dependencies) {
        return UriComponentsBuilder.fromUriString(SPRING_INITIALIZR_API)
                .queryParam(PARAM_TYPE, "maven-project")
                .queryParam(PARAM_LANGUAGE, "java")
                .queryParam(PARAM_BOOT_VERSION, "3.4.4")
                .queryParam(PARAM_BASE_DIR, artifact)
                .queryParam(PARAM_GROUP_ID, group)
                .queryParam(PARAM_ARTIFACT_ID, artifact)
                .queryParam(PARAM_NAME, artifact)
                .queryParam(PARAM_DESCRIPTION, description)
                .queryParam(PARAM_PACKAGE_NAME, packageName)
                .queryParam(PARAM_PACKAGING, packaging)
                .queryParam(PARAM_JAVA_VERSION, javaVersion)
                .queryParam(PARAM_DEPENDENCIES, dependencies)
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
            throw new RuntimeException("Error al generar el proyecto desde Spring Initializr, Código de respuesta: "
                    + response.getStatusCode());
        }
    }
}
