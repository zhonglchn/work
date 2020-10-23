package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;


    /**
     * 本地上传图片
     */
    @PostMapping("/image")
    public ResponseEntity<String> uploadImgToNginx(@RequestParam("file") MultipartFile file){
        String imgUrl = uploadService.uploadImgToNginx(file);
        return ResponseEntity.ok(imgUrl);
    }


    /**
     * 获取阿里云OSS签名
     */
    @GetMapping("/signature")
    public ResponseEntity<Map<String, Object>> getSignature(){
        Map<String, Object> result = uploadService.getSignature();
        return ResponseEntity.ok(result);
    }
    
}