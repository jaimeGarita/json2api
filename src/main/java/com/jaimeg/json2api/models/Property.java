package com.jaimeg.json2api.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Property {

    private String name;

    private String type;

    private String pkg;

    @JsonAlias("primary_key")
    private Boolean isPrimaryKey;

    @JsonAlias("relation_type")
    private String relationType; //OneToMany, OneToOne, ManyToMany

    @Column(name = "id_fk")
    private String idFk;

}
