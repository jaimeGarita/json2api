package com.jaimeg.json2api.util.strategy;

import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;

import com.jaimeg.json2api.enums.CommonJavaType;
import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.Property;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Component
public class ModelClassGenerator implements ComponentStrategy {

    @Override
    public String generate(EntityStructure entityStructure, String group, String artifact) {

        String packageName = group + "." + artifact + ".model";
        String className = entityStructure.getName();
        List<Property> fields = entityStructure.getProperties();

        TypeSpec.Builder classBuilder = this.getClassBuilder(className);
        MethodSpec.Builder constructBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        this.constructClass(fields, classBuilder, constructBuilder, className, packageName);

        classBuilder.addMethod(constructBuilder.build());
        TypeSpec generatedClass = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(packageName, generatedClass).build();

        StringWriter out = new StringWriter();

        try {
            javaFile.writeTo(out);
        } catch (Exception e) {
            throw new UncheckedIOException(new java.io.IOException("Failed to generate class", e));
        }

        return out.toString();

    }

    private String convertPkg(String pkg) {
        return pkg.substring(0, pkg.lastIndexOf("."));
    }

    private TypeSpec.Builder getClassBuilder(String className) {
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addJavadoc("This is the model for" + className + "entity. \n")
                .addAnnotation(AnnotationSpec.builder(Table.class).addMember("name", "$S", className).build());

    }

    private void constructClass(List<Property> fields, TypeSpec.Builder classBuilder, MethodSpec.Builder consterBuilder,
            String className, String packageName) {

        fields.forEach((field) -> {
            String fieldName = field.getName();
            String fieldTypeStr = field.getType();
            String fieldRelationShip = field.getRelationType();
            boolean isPrimaryKey = field.getIsPrimaryKey() != null && field.getIsPrimaryKey();

            if (fieldRelationShip != null && field.getPkg() == null) {
                field.setPkg(packageName);
            }
            TypeName fieldType = mapType(fieldTypeStr, field.getPkg());

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);
            if (isPrimaryKey) {
                this.getPrimaryKey(fieldSpecBuilder);
            }
            if (fieldRelationShip != null && !fieldRelationShip.isEmpty()) {
                this.getRelationShips(field, fieldSpecBuilder, className);
            }
            FieldSpec fieldSpec = fieldSpecBuilder.build();

            this.addGetter(fieldSpec, classBuilder);
            this.addSetter(fieldSpec, classBuilder);

            this.addConstruct(fieldSpec, classBuilder, consterBuilder, fieldName, fieldType);

        });

    }

    private void addConstruct(FieldSpec fieldSpec, TypeSpec.Builder classBuilder, MethodSpec.Builder constructBuilder,
            String fieldName, TypeName fieldType) {
        classBuilder.addField(fieldSpec);

        constructBuilder
                .addParameter(fieldType, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName);
    }

    private TypeName mapType(String type, String pkg) {

        if (type.startsWith("List<") && type.endsWith(">")) {
            String genericTypeStr = type.substring(type.indexOf("<") + 1, type.indexOf(">"));

            TypeName genericType = mapType(genericTypeStr, pkg);
            return ParameterizedTypeName.get(ClassName.get("java.util", "List"), genericType);
        }

        return CommonJavaType.fromName(type)
                .map(CommonJavaType::getClassName)
                .orElseGet(() -> {
                    if (pkg != null && pkg.contains(".")) {
                        String convertPkg = this.convertPkg(pkg);
                        if (type.contains(pkg)) {
                            return ClassName.get(convertPkg, type);
                        }
                        return ClassName.get(convertPkg + ".model", type);
                    }
                    return ClassName.get("java.lang", type);
                });
    }

    private void getPrimaryKey(FieldSpec.Builder fieldSpecBuilder) {
        fieldSpecBuilder
                .addAnnotation(Id.class)
                .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                        .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                        .build());
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
                fieldSpecBuilder.addAnnotation(AnnotationSpec.builder(JoinTable.class)
                        .addMember("name", "$S", property.getName().toLowerCase())
                        .addMember("joinColumns", "$L", AnnotationSpec.builder(JoinColumn.class)
                                .addMember("name", "$S", className.toLowerCase() + "_id")
                                .build())
                        .addMember("inverseJoinColumns", "$L", AnnotationSpec.builder(JoinColumn.class)
                                .addMember("name", "$S",
                                        property.getIdFk().isEmpty() ? property.getName().toLowerCase() + "_id"
                                                : property.getIdFk())
                                .build())
                        .build());
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

    private void addSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String setterName = "set" + capitalizeFirstLetter(fieldSpec.name);
        MethodSpec.Builder methoBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC);
        methoBuilder.addParameter(fieldSpec.type, fieldSpec.name);
        methoBuilder.addStatement("this." + fieldSpec.name + "=" + fieldSpec.name);
        classBuilder.addMethod(methoBuilder.build());
    }

    private void addGetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String getterName = "get" + capitalizeFirstLetter(fieldSpec.name);
        MethodSpec.Builder methoBuilder = MethodSpec.methodBuilder(getterName).returns(fieldSpec.type)
                .addModifiers(Modifier.PUBLIC);
        methoBuilder.addStatement("return this." + fieldSpec.name);
        classBuilder.addMethod(methoBuilder.build());
    }

    private static String capitalizeFirstLetter(final String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
