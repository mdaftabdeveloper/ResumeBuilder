package com.resumebuilder.resumebuilderapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;
    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {
        log.info("Inside FileUploadService: uploadSingleImage() {}", file );
        Map<String, Object> imageUploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type","image"));
        return Map.of("imageUrl", imageUploadResult.get("secure_url").toString());

    }
}