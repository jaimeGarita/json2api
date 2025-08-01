package com.jaimeg.json2api.controller;


import com.jaimeg.json2api.models.JsonTransformer;
import com.jaimeg.json2api.service.CodeGeneratorService;
import com.jaimeg.json2api.util.SpringInitializrUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api")
@CrossOrigin
public class CodeGeneratorController {

    private final CodeGeneratorService codeGeneratorService;
    private final SpringInitializrUtil springInitializrUtil;

    public CodeGeneratorController(CodeGeneratorService codeGeneratorService, SpringInitializrUtil springInitializrUtil) {
        this.codeGeneratorService = codeGeneratorService;
        this.springInitializrUtil = springInitializrUtil;
    }

    
    @PostMapping("")
    public ResponseEntity<byte[]> generateCode(@RequestBody JsonTransformer models) {
        try {
            byte[] zipBytes = this.springInitializrUtil.generateProjectFromInitializr(
                    models.getGroup(),
                    models.getArtifact(),
                    models.getGroup() + "." + models.getArtifact(),
                    models.getDependencies(),
                    models.getDescription(),
                    models.getJavaVersion(),
                    models.getPackaging().toLowerCase()
            );
            byte[] finalZip = this.codeGeneratorService.generateCodeService(models, zipBytes);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=project.zip")
                    .body(finalZip);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
