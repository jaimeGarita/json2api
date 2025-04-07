package com.jaimeg.json2api.generator;

import com.jaimeg.json2api.models.EntityStructure;
import com.jaimeg.json2api.models.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class JsonTransformerClassGeneratorTest {

    private ModelClassGenerator modelClassGenerator;

    @BeforeEach
    public void setUp() {
        modelClassGenerator = new ModelClassGenerator();
    }

    @Test
    void test_generateModelClassCode() {
        EntityStructure model = createSampleModel();
        String code = modelClassGenerator.generateModelClassCode(model, "com.example", "prueba");

        assertClassStructure(code);
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertConstructor(code, model.getProperties());
    }

    @Test
    void test_generateModelWithPkg() {
        EntityStructure model = createModelWithPkg();
        String code = modelClassGenerator.generateModelClassCode(model, "com.example", "prueba");

        assertClassStructure(code);
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertFieldAnnotations(code, "createdAt", "LocalDateTime", false);
        assertConstructor(code, model.getProperties());
    }

    @Test
    void test_generateModelWithManyToOne() {
        EntityStructure entityStructure = createModelsWithRelationShips("ManyToOne", null);
        String code = modelClassGenerator.generateModelClassCode(entityStructure, "com.example", "prueba");
        assertClassStructure(code);
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertFieldAnnotations(code, "post", "Post", false);
        assertManyToOne(code);
        assertConstructor(code, entityStructure.getProperties());

    }

    @Test
    void test_generateModelWithOneToMany() {
        EntityStructure entityStructure = createModelsWithRelationShips("OneToMany", null);
        String code = modelClassGenerator.generateModelClassCode(entityStructure, "com.example", "prueba");
        assertClassStructure(code);
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertFieldAnnotations(code, "posts", "List<Post>", false);
        assertOneToMany(code);
        assertConstructor(code, entityStructure.getProperties());

    }

    @Test
    void test_generateModelWithManyToMany() {
        EntityStructure entityStructure = createModelsWithRelationShips("ManyToMany", "post_id");
        String code = modelClassGenerator.generateModelClassCode(entityStructure, "com.example", "prueba");
        assertClassStructure(code);
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertFieldAnnotations(code, "posts", "List<Post>", false);
        assertManyToMany(code);
        assertConstructor(code, entityStructure.getProperties());

    }


    private EntityStructure createSampleModel() {
        EntityStructure model = new EntityStructure();
        model.setName("User");
        List<Property> properties = new ArrayList<>();
        properties.add(Property.builder().name("id").type("Integer").isPrimaryKey(true).build());
        properties.add(Property.builder().name("name").type("String").build());
        properties.add(Property.builder().name("age").type("Integer").build());
        model.setProperties(properties);
        return model;
    }

    private EntityStructure createModelsWithRelationShips(String relationShip, String idFk) {
        EntityStructure model = new EntityStructure();
        model.setName("User");
        List<Property> properties = new ArrayList<>();
        properties.add(Property.builder().name("id").type("Integer").isPrimaryKey(true).build());
        properties.add(Property.builder().name("name").type("String").build());
        properties.add(Property.builder().name("age").type("Integer").build());
        if ("ManyToOne".equals(relationShip))
            properties.add(Property.builder().name("post").type("Post").pkg("com.test.model.Post").relationType(relationShip).build());
        else
            properties.add(Property.builder().name("posts").type("List<Post>").pkg("com.test.model.Post").relationType(relationShip).idFk(idFk).build());
        model.setProperties(properties);

        return model;

    }

    private void assertManyToOne(String code) {
        String manyToOneAnnotation =
                """
                        @ManyToOne
                        @JoinColumn(
                            name = "post_id"
                        )
                        private Post post;
                        """;
        manyToOneAnnotation = manyToOneAnnotation.replaceAll("\\s+", " ").trim();
        Assertions.assertTrue(code.replaceAll("\\s+", " ").contains(manyToOneAnnotation));
    }

    private void assertOneToMany(String code) {
        String oneToManyAnnotation =
                """
                        @OneToMany(
                            mappedBy = "User"
                        )
                        private List<Post> posts;
                        """;
        oneToManyAnnotation = oneToManyAnnotation.replaceAll("\\s+", " ").trim();
        Assertions.assertTrue(code.replaceAll("\\s+", " ").contains(oneToManyAnnotation));
    }

    private void assertManyToMany(String code) {
        String oneToManyAnnotation =
                """
                                  @ManyToMany
                                  @JoinTable(
                                      name = "posts",
                                      joinColumns = @JoinColumn(name = "user_id"),
                                      inverseJoinColumns = @JoinColumn(name = "post_id")
                                  )
                                  private List<Post> posts;
                        """;
        oneToManyAnnotation = oneToManyAnnotation.replaceAll("\\s+", " ").trim();
        Assertions.assertTrue(code.replaceAll("\\s+", " ").contains(oneToManyAnnotation));
    }

    private void assertClassStructure(String code) {
        Assertions.assertTrue(code.contains("public class " + "User"));
        Assertions.assertTrue(code.contains("@Table(\n" + "    name = \"" + "User" + "\"\n" + ")"));
    }

    private void assertEntityAnnotations(String code) {
        Assertions.assertTrue(code.contains("import jakarta.persistence.Entity"));
        Assertions.assertTrue(code.contains("@Entity"));
        Assertions.assertTrue(code.contains("import jakarta.persistence.Table"));
    }

    private void assertFieldAnnotations(String code, String fieldName, String fieldType, boolean isId) {
        String fieldTypeWithModifiers = "private " + fieldType + " " + fieldName + ";";
        Assertions.assertTrue(code.contains(fieldTypeWithModifiers));

        if (isId) {
            Assertions.assertTrue(code.contains("@Id"));
            Assertions.assertTrue(code.contains("@GeneratedValue(\n" +
                    "      strategy = GenerationType.IDENTITY\n" +
                    "  )"));
        }
    }

    private void assertConstructor(String code, List<Property> properties) {
        StringBuilder expectedConstructor = new StringBuilder();
        expectedConstructor.append(" public User(");
        int size = properties.size();
        IntStream.range(0, size).forEach(i -> {
            Property prop = properties.get(i);
            expectedConstructor.append(prop.getType()).append(" ").append(prop.getName());
            if (i < size - 1) {
                expectedConstructor.append(", ");
            }
        });
        expectedConstructor.append(") {\n");
        properties.forEach((prop) -> {
            expectedConstructor.append("    this.").append(prop.getName()).append(" = ").append(prop.getName()).append(";\n");
        });
        expectedConstructor.append("  }");

        Assertions.assertTrue(code.contains(expectedConstructor.toString()));
    }

    private EntityStructure createModelWithPkg() {
        EntityStructure model = this.createSampleModel();
        List<Property> properties = model.getProperties();
        properties.add(Property.builder().name("createdAt").type("LocalDateTime").pkg("java.time.LocalDateTime").build());
        model.setProperties(properties);

        return model;
    }
}
