package com.jaimeg.json2api.controller;


import com.jaimeg.json2api.generator.ModelClassGenerator;
import com.jaimeg.json2api.models   .ModelStruct;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api")
@CrossOrigin
public class CodeGeneratorController {


    private final ModelClassGenerator modelClassGenerator;

    public CodeGeneratorController(ModelClassGenerator modelClassGenerator) {
        this.modelClassGenerator = modelClassGenerator;
    }

    @PostMapping()
    public ResponseEntity<byte[]> generateCode(@RequestBody ModelStruct models) {

        String code = modelClassGenerator.generateModelClassCode(models);
        try {
            // Imprimir el contenido del código generado antes de enviarlo como archivo
            System.out.println("Contenido del código generado: " + code);

            // Convertir el código generado a un array de bytes
            byte[] fileContent = code.getBytes(StandardCharsets.UTF_8);

            // Comprobar si el contenido está vacío
            if (fileContent.length == 0) {
                throw new IOException("El contenido generado está vacío.");
            }

            // Establecer los encabezados para la descarga del archivo
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=GeneratedModel.java");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(("Error al generar el archivo: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
