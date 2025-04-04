package com.jaimeg.json2api.enums;

import com.squareup.javapoet.ClassName;

import java.util.Arrays;
import java.util.Optional;

public enum CommonJavaType {
    INT("int", Integer.class),
    LONG("long", Long.class),
    DOUBLE("double", Double.class),
    BOOLEAN("boolean", Boolean.class),
    STRING("String", String.class),
    BIGDECIMAL("BigDecimal", "java.math"),
    UUID("UUID", "java.util"),
    LOCALDATE("LocalDate", "java.time");

    private final String name;
    private final ClassName className;

    CommonJavaType(String name, Class<?> clazz) {
        this.name = name;
        this.className = ClassName.get(clazz);
    }

    CommonJavaType(String name, String packageName) {
        this.name = name;
        this.className = ClassName.get(packageName, name);
    }
    public ClassName getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public static Optional<CommonJavaType> fromName(String typeName) {
        return Arrays.stream(values())
                .filter(t -> t.getName().equalsIgnoreCase(typeName))
                .findFirst();
    }

}
