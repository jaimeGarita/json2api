package com.jaimeg.json2api.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonTransformer {

    private String group;

    private String artifact;

    private String description;

    @JsonAlias("java_version")
    private String javaVersion;

    @JsonAlias("packaging")
    private String packaging;

    private String dependencies;

    @JsonAlias("models")
    private List<EntityStructure> models;

    @JsonAlias("components")
    private GenerationOptions generationOptions;
}
