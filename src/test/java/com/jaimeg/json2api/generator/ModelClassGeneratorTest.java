package com.jaimeg.json2api.generator;

import com.jaimeg.json2api.models.ModelStruct;
import com.jaimeg.json2api.models.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class ModelClassGeneratorTest {

    private ModelClassGenerator modelClassGenerator;

    @BeforeEach
    public void setUp() {
        modelClassGenerator = new ModelClassGenerator();
    }

    @Test
    void test_generateModelClassCode() {
        ModelStruct model = createSampleModel();
        String code = modelClassGenerator.generateModelClassCode(model);

        assertClassStructure(code, "User");
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertConstructor(code, model.getProperties());
    }

    private ModelStruct createSampleModel() {
        ModelStruct model = new ModelStruct();
        model.setName("User");
        List<Property> properties = new ArrayList<>();
        properties.add(new Property("id", "Integer", null, true));
        properties.add(new Property("name", "String", null, false));
        properties.add(new Property("age", "Integer", null, false));
        model.setProperties(properties);
        return model;
    }

    private void assertClassStructure(String code, String className) {
        Assertions.assertTrue(code.contains("public class " + className));
        Assertions.assertTrue(code.contains("@Table(\n" + "    name = \"" + className + "\"\n" + ")"));
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


    private ModelStruct createModelWithPkg(){
        ModelStruct model = this.createSampleModel();
        List<Property> properties = model.getProperties();
        properties.add(new Property("createdAt", "LocalDateTime", "java.time.LocalDateTime", false));
        model.setProperties(properties);

        return model;
    }


    @Test
    void test_generateModelWithPkg() {
        ModelStruct model = createModelWithPkg();
        String code = modelClassGenerator.generateModelClassCode(model);

        assertClassStructure(code, "User");
        assertEntityAnnotations(code);
        assertFieldAnnotations(code, "id", "Integer", true);
        assertFieldAnnotations(code, "name", "String", false);
        assertFieldAnnotations(code, "age", "Integer", false);
        assertFieldAnnotations(code, "createdAt", "LocalDateTime", false);
        assertConstructor(code, model.getProperties());
    }
}
