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

        this.constructClass(fields, classBuilder, constructBuilder, className);

        classBuilder.addMethod(constructBuilder.build());
        TypeSpec generatedClass = classBuilder.build();
        //TODO CHANGE COM.GENERATED.MODELS BUILDER TO DYNAMIC
        JavaFile javaFile = JavaFile.builder("com.generated.models", generatedClass).build();

        StringWriter out = new StringWriter();
        try {
            javaFile.writeTo(out);
        } catch (Exception e) {
            throw new UncheckedIOException(new java.io.IOException("Failed to generate class", e));
        }

        return out.toString();
    }

    private TypeName mapType(String type, String pkg) {
        //TODO CUANDO TENGA EL PACKAGE, METER AQUI EL PACKAGE PARA LAS RELACIONES
        if (type.startsWith("List<") && type.endsWith(">")) {
            String genericTypeStr = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
            TypeName genericType = mapType(genericTypeStr, pkg);
            return ParameterizedTypeName.get(ClassName.get("java.util", "List"), genericType);
        }

        return CommonJavaType.fromName(type)
                .map(CommonJavaType::getClassName)
                .orElseGet(() -> {
                    if (pkg != null && pkg.contains(".")) {
                        String[] parts = pkg.split("\\.");
                        String className = parts[parts.length - 1];
                        String convertPkg = pkg.substring(0, pkg.lastIndexOf('.'));
                        return ClassName.get(convertPkg, className);
                    }
                    return ClassName.get("java.lang", type);
                });
    }

    private TypeSpec.Builder getClassBuilder(String className) {
        return TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)

                .addJavadoc("This is the model for" + className + "entity. \n")
                .addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("name", "$S", className)
                        .build());
    }

    private void constructClass(List<Property> fields, TypeSpec.Builder classBuilder, MethodSpec.Builder constructBuilder, String className) {
        fields.forEach((field) -> {
            String fieldName = field.getName();
            String fieldTypeStr = field.getType();
            String fieldRelationShip = field.getRelationType();
            boolean isPrimaryKey = field.getIsPrimaryKey() != null && field.getIsPrimaryKey();
            TypeName fieldType = mapType(fieldTypeStr, field.getPkg());

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);
            if (isPrimaryKey) {
                this.getPrimaryKey(fieldSpecBuilder);
            }
            if (fieldRelationShip != null && !fieldRelationShip.isEmpty()) {
                this.getRelationShips(field, fieldSpecBuilder, className);
            }
            FieldSpec fieldSpec = fieldSpecBuilder.build();

            // -- GETTER & SETTER --
            this.addGetter(fieldSpec, classBuilder);
            this.addSetter(fieldSpec, classBuilder);

            // -- CONSTRUCT --
            this.addConstruct(fieldSpec, classBuilder, constructBuilder, fieldName, fieldType);


        });
    }

    private void getPrimaryKey(FieldSpec.Builder fieldSpecBuilder) {
        fieldSpecBuilder
                .addAnnotation(Id.class)
                .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                        .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                        .build());
    }

    private void addConstruct(FieldSpec fieldSpec, TypeSpec.Builder classBuilder, MethodSpec.Builder constructBuilder, String fieldName, TypeName fieldType) {
        classBuilder.addField(fieldSpec);

        constructBuilder
                .addParameter(fieldType, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName);
    }

    private void addSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String setterName = "set" + capitalizeFirstLetter(fieldSpec.name);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC);
        methodBuilder.addParameter(fieldSpec.type, fieldSpec.name);
        methodBuilder.addStatement("this." + fieldSpec.name + "=" + fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }

    public void addGetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String getterName  = "get"+capitalizeFirstLetter(fieldSpec.name);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(getterName).returns(fieldSpec.type).addModifiers(Modifier.PUBLIC);
        methodBuilder.addStatement("return this."+fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }


    private void getRelationShips(Property property, FieldSpec.Builder fieldSpecBuilder, String className) {
        switch (property.getRelationType()) {
            case "OneToMany":
                fieldSpecBuilder.addAnnotation(AnnotationSpec.builder(OneToMany.class)
                        .addMember("mappedBy", "$S", className != null ? className : "unknown")
                        .build());
                break;
            case "ManyToMany":
                fieldSpecBuilder.addAnnotation(ManyToMany.class);
                break;
            case "ManyToOne":
                fieldSpecBuilder.addAnnotation(ManyToOne.class);
                fieldSpecBuilder.addAnnotation(AnnotationSpec.builder(JoinColumn.class)
                        .addMember("name", "$S", property.getName() + "_id")
                        .build());
                break;
            case "OneToOne":
                fieldSpecBuilder.addAnnotation(OneToOne.class);
                break;

        }
    }

    private static String capitalizeFirstLetter(final String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
