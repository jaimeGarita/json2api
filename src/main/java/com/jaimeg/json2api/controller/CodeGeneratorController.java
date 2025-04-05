package com.jaimeg.json2api.controller;


import com.jaimeg.json2api.generator.ModelClassGenerator;
import com.jaimeg.json2api.models.Model;
import com.jaimeg.json2api.models.ModelStruct;
import com.jaimeg.json2api.service.CodeGeneratorService;
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


    private final CodeGeneratorService codeGeneratorService;

    public CodeGeneratorController(CodeGeneratorService codeGeneratorService) {
        this.codeGeneratorService = codeGeneratorService;
    }

    @PostMapping()
    public ResponseEntity<byte[]> generateCode(@RequestBody Model models) {

        try {

            this.codeGeneratorService.generateCodeService(models.getModels());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
