package com.jaimeg.json2api.util.strategy;

import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import com.jaimeg.json2api.models.EntityStructure;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

@Component
public class ServiceClassGenerator implements ComponentStrategy {

        @Override
        public String generate(EntityStructure entityStructure, String group, String artifact) {
                String packageName = group + "." + artifact + ".service";

                String entityName = entityStructure.getName();
                String serviceName = entityName + "Service";
                String repositoryName = entityName + "Repository";

                TypeSpec.Builder classBuilder = this.generateServiceClass(serviceName, entityName);

                ClassName repoClass = ClassName.get(group + "." + artifact + ".repository", repositoryName);
                FieldSpec repoField = FieldSpec
                                .builder(repoClass, decapitalize(repositoryName), Modifier.PRIVATE, Modifier.FINAL)
                                .build();

                classBuilder.addField(repoField);

                classBuilder.addMethod(
                                this.generateConstruct(repositoryName, repoClass));

                ClassName entityClass = ClassName.get(group + "." + artifact + ".model", entityName);

                classBuilder = this.findAll(classBuilder, repositoryName, entityClass);
                classBuilder = this.constructFindById(classBuilder, entityClass, repositoryName);
                classBuilder = this.constructSave(classBuilder, entityClass, repositoryName, entityName);
                classBuilder = this.constructDelete(classBuilder, repositoryName);

                JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
                StringWriter out = new StringWriter();

                try {
                        javaFile.writeTo(out);
                } catch (Exception e) {
                        throw new UncheckedIOException(new java.io.IOException("Failed to generate service class", e));
                }

                return out.toString();
        }

        private TypeSpec.Builder constructFindById(TypeSpec.Builder classBuilder, ClassName entityClass,
                        String repositoryName) {

                ClassName optionalClass = ClassName.get(Optional.class);
                ParameterizedTypeName optionalEntity = ParameterizedTypeName.get(optionalClass, entityClass);

                return classBuilder.addMethod(MethodSpec.methodBuilder("findById")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(optionalEntity)
                                .addParameter(Long.class, "id")
                                .addStatement("return $N.findById(id)", decapitalize(repositoryName))
                                .build());
        }

        private TypeSpec.Builder constructSave(TypeSpec.Builder classBuilder, ClassName entityClass,
                        String repositoryName, String entityName) {

                return classBuilder.addMethod(MethodSpec.methodBuilder("save")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(entityClass)
                                .addParameter(entityClass, decapitalize(entityName))
                                .addStatement("return $N.save($N)", decapitalize(repositoryName),
                                                decapitalize(entityName))
                                .build());
        }

        private MethodSpec generateConstruct(String repositoryName, ClassName repoClass) {
                return MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(repoClass, decapitalize(repositoryName))
                                .addStatement("this.$N = $N", decapitalize(repositoryName),
                                                decapitalize(repositoryName))
                                .build();
        }

        private TypeSpec.Builder findAll(TypeSpec.Builder classBuilder, String repositoryName, ClassName entityClass) {
                ClassName listClass = ClassName.get(List.class);
                ParameterizedTypeName listOfEntity = ParameterizedTypeName.get(listClass, entityClass);

                return classBuilder.addMethod(MethodSpec.methodBuilder("findAll")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(listOfEntity)
                                .addStatement("return $N.findAll()", decapitalize(repositoryName))
                                .build());
        }

        private TypeSpec.Builder constructDelete(TypeSpec.Builder classBuilder,
                        String repositoryName) {

                return classBuilder.addMethod(MethodSpec.methodBuilder("delete")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addParameter(Long.class, "id")
                                .addStatement("$N.deleteById(id)", decapitalize(repositoryName))
                                .build());
        }

        private TypeSpec.Builder generateServiceClass(String serviceName, String entityName) {
                return TypeSpec.classBuilder(serviceName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Repository.class)
                                .addJavadoc("""
                                                Spring Data JPA repository for the {@code $L} entity.
                                                <p>
                                                Provides CRUD operations via {@link JpaRepository}.
                                                """, entityName);
        }

        private static String decapitalize(String str) {
                return str.substring(0, 1).toLowerCase() + str.substring(1);
        }

}
