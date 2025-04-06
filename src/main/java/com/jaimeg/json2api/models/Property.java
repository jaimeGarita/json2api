package com.jaimeg.json2api.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    private String name;

    private String type;

    private String pkg;

    @JsonAlias("primary_key")
    private Boolean isPrimaryKey;

    private String relationType; //OneToMany, OneToOne, ManyToMany

}
