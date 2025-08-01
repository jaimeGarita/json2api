package com.jaimeg.json2api.util.strategy;

import java.io.StringWriter;
import java.io.UncheckedIOException;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaimeg.json2api.models.EntityStructure;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

@Component
public class ControllerClassGenerator implements ComponentStrategy {

    @Override
    public String generate(EntityStructure entityStructure, String group, String artifact) {
        String basePackage = group + "." + artifact;
        String packageName = basePackage + ".controller";
        String entityName = entityStructure.getName();
        String controllerName = entityName + "Controller";


        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(controllerName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "$S", "/" + entityName.toLowerCase())
                        .build())
                .addJavadoc("""
                        REST controller for {@code $L}.
                        <p>
                        Exposes basic REST endpoints for the entity.
                        """, entityName);

        MethodSpec getAllMethod = constructGetAllUserClass(entityName);
        MethodSpec postUserMethod = constructPostUserClass(entityName);
        MethodSpec putUserMethod = constructPutUserClass(entityName);
        MethodSpec patchUserMethod = constructPatchUserClass(entityName);
        MethodSpec deletedUserMethor = constructDeleteUserClass(entityName);

        classBuilder.addMethod(getAllMethod);
        classBuilder.addMethod(postUserMethod);
        classBuilder.addMethod(putUserMethod);
        classBuilder.addMethod(patchUserMethod);
        classBuilder.addMethod(deletedUserMethor);

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

    public MethodSpec constructGetAllUserClass(String entityName) {
        return MethodSpec.methodBuilder("getAll" + entityName + "s")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember("value", "$S", "/")
                        .build())
                .returns(String.class)
                .addStatement("return $S", "Listado de " + entityName)
                .build();
    }

    public MethodSpec constructPostUserClass(String entityName) {
        return MethodSpec.methodBuilder("create" + entityName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                        .addMember("value", "$S", "/")
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.bestGuess(entityName), "body")
                        .addAnnotation(ClassName.get(RequestBody.class))
                        .build())
                .returns(String.class)
                .addStatement("return $S", "Creado " + entityName)
                .build();
    }

    public MethodSpec constructPutUserClass(String entityName) {
        return MethodSpec.methodBuilder("update" + entityName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(Long.class), "id")
                        .addAnnotation(PathVariable.class)
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.bestGuess(entityName), "body")
                        .addAnnotation(ClassName.get(RequestBody.class))
                        .build())
                .returns(String.class)
                .addStatement("return $S", "Actualizado " + entityName + " con ID: \" + id")
                .build();
    }

    public MethodSpec constructPatchUserClass(String entityName) {
        return MethodSpec.methodBuilder("partialUpdate" + entityName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(PatchMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(Long.class), "id")
                        .addAnnotation(ClassName.get(PathVariable.class))
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.bestGuess(entityName), "body")
                        .addAnnotation(ClassName.get(RequestBody.class))
                        .build())
                .returns(String.class)
                .addStatement("return $S", "Parcialmente actualizado " + entityName + " con ID: \" + id")
                .build();
    }

    public MethodSpec constructDeleteUserClass(String entityName) {
        return MethodSpec.methodBuilder("delete" + entityName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(Long.class), "id")
                        .addAnnotation(ClassName.get(PathVariable.class))
                        .build())
                .returns(String.class)
                .addStatement("return $S", "Eliminado " + entityName + " con ID: \" + id")
                .build();
    }

}
