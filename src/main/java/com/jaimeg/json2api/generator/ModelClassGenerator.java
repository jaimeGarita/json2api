package com.jaimeg.json2api.generator;


import com.jaimeg.json2api.enums.CommonJavaType;
import com.jaimeg.json2api.models.ModelStruct;
import com.jaimeg.json2api.models.Property;
import com.squareup.javapoet.*;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ModelClassGenerator {

    public String generateModelClassCode(ModelStruct modelStruct) {
        String className = modelStruct.getName();
        List<Property> fields = modelStruct.getProperties();

        TypeSpec.Builder classBuilder = this.getClassBuilder(className);
        MethodSpec.Builder constructBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        this.getConstructBuilder(fields, classBuilder, constructBuilder);

        classBuilder.addMethod(constructBuilder.build());
        TypeSpec generatedClass = classBuilder.build();
        JavaFile javaFile = JavaFile.builder("com.generated.models", generatedClass).build();

        StringWriter out = new StringWriter();
        try {
            javaFile.writeTo(out);
        } catch (Exception e) {
            throw new UncheckedIOException(new java.io.IOException("Failed to generate class", e));
        }

        return out.toString();
    }

    private ClassName mapType(String type) {
        return CommonJavaType.fromName(type)
                .map(CommonJavaType::getClassName)
                .orElseGet(() -> {
                    if (type.contains(".")) {
                        String[] parts = type.split("\\.");
                        String className = parts[parts.length - 1];
                        String pkg = type.substring(0, type.lastIndexOf('.'));
                        return ClassName.get(pkg, className);
                    }
                    return ClassName.get("java.lang", type);
                });
    }

    private TypeSpec.Builder getClassBuilder(String className){
        return TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addJavadoc("This is the model for" + className + "entity. \n")
                .addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("name", "$S", className)
                        .build());
    }

    private void getConstructBuilder(List<Property> fields, TypeSpec.Builder classBuilder,  MethodSpec.Builder constructBuilder){
        fields.forEach((field) -> {
            String fieldName = field.getName();
            String fieldTypeStr = field.getType();
            Boolean isPrimaryKey = field.getIsPrimaryKey() != null && field.getIsPrimaryKey();
            ClassName fieldType = mapType(fieldTypeStr);

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);
            if (isPrimaryKey){
                fieldSpecBuilder
                        .addAnnotation(Id.class)
                        .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                                .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                                .build());
            }

            FieldSpec fieldSpec = fieldSpecBuilder.build();
            classBuilder.addField(fieldSpec);

            constructBuilder
                    .addParameter(fieldType, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName);

        });
    }



}
