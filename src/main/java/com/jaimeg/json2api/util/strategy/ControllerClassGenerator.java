package com.jaimeg.json2api.util.strategy;

import java.io.StringWriter;
import java.io.UncheckedIOException;
import javax.lang.model.element.Modifier;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import com.jaimeg.json2api.models.EntityStructure;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@Component
public class ControllerClassGenerator implements ComponentStrategy {

        @Override
        public String generate(EntityStructure entityStructure, String group, String artifact) {
                String basePackage = group + "." + artifact;
                String packageName = basePackage + ".controller";
                String entityName = entityStructure.getName();
                String controllerName = entityName + "Controller";
                String serviceName = entityName + "Service";

                ClassName serviceClass = ClassName.get(basePackage + ".service", serviceName);
                ClassName entityClass = ClassName.get(basePackage + ".model", entityName);

                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(controllerName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(RestController.class)
                                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                                                .addMember("value", "$S", "/" + entityName.toLowerCase())
                                                .build())
                                .addJavadoc("""
                                                REST controller for {@code $L}.
                                                <p>
                                                Exposes CRUD endpoints for the entity.
                                                """, entityName);

                FieldSpec serviceField = FieldSpec.builder(serviceClass, decapitalize(serviceName),
                                Modifier.PRIVATE, Modifier.FINAL).build();
                classBuilder.addField(serviceField);

                MethodSpec constructor = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(serviceClass, decapitalize(serviceName))
                                .addStatement("this.$N = $N", decapitalize(serviceName), decapitalize(serviceName))
                                .build();
                classBuilder.addMethod(constructor);

                classBuilder.addMethod(constructGetAllMethod(entityName, serviceName, entityClass));
                classBuilder.addMethod(constructGetByIdMethod(entityName, serviceName, entityClass));
                classBuilder.addMethod(constructPostMethod(entityName, serviceName, entityClass));
                classBuilder.addMethod(constructPutMethod(entityName, serviceName, entityClass));
                classBuilder.addMethod(constructDeleteMethod(entityName, serviceName, entityClass));

                TypeSpec controllerClass = classBuilder.build();
                JavaFile javaFile = JavaFile.builder(packageName, controllerClass).build();

                StringWriter out = new StringWriter();
                try {
                        javaFile.writeTo(out);
                } catch (Exception e) {
                        throw new UncheckedIOException(new java.io.IOException("Failed to generate controller", e));
                }

                return out.toString();
        }

        private MethodSpec constructGetAllMethod(String entityName, String serviceName, ClassName entityClass) {
                return MethodSpec.methodBuilder("getAll" + entityName + "s")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(GetMapping.class)
                                .returns(ParameterizedResponseList(entityClass))
                                .addStatement("return $T.ok($N.findAll())", ResponseEntity.class,
                                                decapitalize(serviceName))
                                .build();
        }

        private MethodSpec constructGetByIdMethod(String entityName, String serviceName, ClassName entityClass) {
                return MethodSpec.methodBuilder("get" + entityName + "ById")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                                                .addMember("value", "$S", "/{id}")
                                                .build())
                                .addParameter(ParameterSpec.builder(Long.class, "id")
                                                .addAnnotation(PathVariable.class).build())
                                .returns(ParameterizedResponse(entityClass))
                                .addStatement("return $T.of($N.findById(id))", ResponseEntity.class,
                                                decapitalize(serviceName))
                                .build();
        }

        private MethodSpec constructPostMethod(String entityName, String serviceName, ClassName entityClass) {
                return MethodSpec.methodBuilder("create" + entityName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(PostMapping.class)
                                .addParameter(ParameterSpec
                                                .builder(ClassName.bestGuess(entityName), decapitalize(entityName))
                                                .addAnnotation(RequestBody.class)
                                                .build())
                                .returns(ParameterizedResponse(entityClass))
                                .addStatement("return $T.ok($N.save($N))", ResponseEntity.class,
                                                decapitalize(serviceName), decapitalize(entityName))
                                .build();
        }

        private MethodSpec constructPutMethod(String entityName, String serviceName, ClassName entityClass) {
                return MethodSpec.methodBuilder("update" + entityName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                                                .addMember("value", "$S", "/{id}")
                                                .build())
                                .addParameter(ParameterSpec.builder(Long.class, "id")
                                                .addAnnotation(PathVariable.class).build())
                                .addParameter(ParameterSpec
                                                .builder(ClassName.bestGuess(entityName), decapitalize(entityName))
                                                .addAnnotation(RequestBody.class).build())
                                .returns(ParameterizedResponse(entityClass))
                                .addStatement("Optional<$L> existing = $N.findById(id)", entityName,
                                                decapitalize(serviceName))
                                .beginControlFlow("if (existing.isEmpty())")
                                .addStatement("return $T.notFound().build()", ResponseEntity.class)
                                .endControlFlow()
                                .addStatement("$N.setId(id)", decapitalize(entityName))
                                .addStatement("return $T.ok($N.save($N))", ResponseEntity.class,
                                                decapitalize(serviceName), decapitalize(entityName))
                                .build();
        }

        private MethodSpec constructDeleteMethod(String entityName, String serviceName, ClassName entityClass) {
                TypeName optionalEntity = ParameterizedTypeName.get(
                                ClassName.get(java.util.Optional.class),
                                entityClass);

                return MethodSpec.methodBuilder("delete" + entityName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                                                .addMember("value", "$S", "/{id}")
                                                .build())
                                .addParameter(ParameterSpec.builder(Long.class, "id")
                                                .addAnnotation(PathVariable.class)
                                                .build())
                                .returns(ResponseEntity.class)
                                .addStatement("$T existing = $N.findById(id)", optionalEntity,
                                                decapitalize(serviceName))
                                .beginControlFlow("if (existing.isEmpty())")
                                .addStatement("return $T.notFound().build()", ResponseEntity.class)
                                .endControlFlow()
                                .addStatement("$N.delete(id)", decapitalize(serviceName))
                                .addStatement("return $T.noContent().build()", ResponseEntity.class)
                                .build();
        }

        private com.squareup.javapoet.TypeName ParameterizedResponseList(ClassName entityClass) {
                return com.squareup.javapoet.ParameterizedTypeName.get(
                                ClassName.get(ResponseEntity.class),
                                com.squareup.javapoet.ParameterizedTypeName.get(
                                                ClassName.get(java.util.List.class),
                                                entityClass));
        }

        private com.squareup.javapoet.TypeName ParameterizedResponse(ClassName entityClass) {
                return com.squareup.javapoet.ParameterizedTypeName.get(
                                ClassName.get(ResponseEntity.class),
                                entityClass);
        }

        private static String decapitalize(String str) {
                return str.substring(0, 1).toLowerCase() + str.substring(1);
        }
}
