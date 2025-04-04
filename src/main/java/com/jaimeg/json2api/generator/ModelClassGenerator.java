package com.jaimeg.json2api.generator;


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

        fields.forEach((field) -> {
            String fieldName = field.getName();
            String fieldTypeStr = field.getType();
            ClassName fieldType = mapType(fieldTypeStr);

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);
            if (fieldName.equals(fields.get(0).getName())) {
                fieldSpecBuilder
                        .addAnnotation(Id.class)
                        .addAnnotation(AnnotationSpec.builder(GeneratedValue.class).addMember("strategy", "$T.IDENTITY", GenerationType.class).build());
            }


            FieldSpec fieldSpec = fieldSpecBuilder.build();
            classBuilder.addField(fieldSpec);

            constructBuilder
                    .addParameter(fieldType, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName);

        });

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

    //TODO REMOVE THIS FUNCTION
    private ClassName mapType(String type) {
        switch (type) {
            case "int":
                return ClassName.get(Integer.class);
            case "long":
                return ClassName.get(Long.class);
            case "double":
                return ClassName.get(Double.class);
            case "boolean":
                return ClassName.get(Boolean.class);
            case "String":
                return ClassName.get(String.class);
            default:
                // Fallback a java.lang.Object
                return ClassName.get("java.lang", type);
        }
    }

    private TypeSpec.Builder getClassBuilder(String className){
        return TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addJavadoc("This is the model for" + className + "entity. \n")
                .addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("name", "$S", className)
                        .build());
    }

    private

}
