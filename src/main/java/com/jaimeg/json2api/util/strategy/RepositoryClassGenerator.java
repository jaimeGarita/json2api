package com.jaimeg.json2api.util.strategy;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.Property;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

@Component
public class RepositoryClassGenerator implements ComponentStrategy {

    @Override
    public String generate(EntityStructure entityStructure, String group, String artifact) {
        String basePackage = group + "." + artifact;
        String packageName = basePackage + ".repository";
        String entityName = entityStructure.getName();
        String repositoryName = entityName + "Repository";

        String entityPackage = basePackage + ".model";

        Optional<Property> primaryKeyProp = entityStructure.getProperties().stream()
                .filter(Property::getIsPrimaryKey)
                .findFirst();

        Optional<String> idTypeOptional = primaryKeyProp.map(Property::getType);
        Optional<String> idPkg = primaryKeyProp.map(Property::getPkg);

        ClassName jpaRepository = ClassName.get("org.springframework.data.jpa.repository", "JpaRepository");
        ClassName entityClass = ClassName.get(entityPackage, entityName);
        ClassName idClass = idTypeOptional.map(type -> {
            switch (type) {
                case "Long":
                case "Integer":
                case "String":
                case "Double":
                case "Float":
                case "Boolean":
                    return ClassName.get("java.lang", type);
                default:
                    if (idPkg.isPresent()) {
                        return ClassName.get(idPkg.get(), type);
                    }
                    return ClassName.get("java.lang", type);
            }
        }).orElseThrow(() -> new IllegalArgumentException("No primary key defined for entity " + entityName));

        ParameterizedTypeName superInterface = ParameterizedTypeName.get(jpaRepository, entityClass, idClass);

        TypeSpec.Builder repositoryInterface = TypeSpec.interfaceBuilder(repositoryName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Repository.class)
                .addSuperinterface(superInterface)
                .addJavadoc("""
                        Spring Data JPA repository for the {@code $L} entity.
                        <p>
                        Provides CRUD operations via {@link JpaRepository}.
                        """, entityName);

        JavaFile javaFile = JavaFile.builder(packageName, repositoryInterface.build()).build();
        StringWriter out = new StringWriter();
        try {
            javaFile.writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException(new IOException("Failed to generate repository", e));
        }

        return out.toString();

    }

}
