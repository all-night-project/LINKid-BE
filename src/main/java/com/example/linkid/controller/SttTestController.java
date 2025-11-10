package com.example.linkid.controller;

import com.example.linkid.service.ClovaSpeechService;
import com.example.linkid.service.ObjectStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class SttTestController {

    private final ObjectStorageService objectStorageService;
    private final ClovaSpeechService clovaSpeechService;

    /**
     * 테스트 1: Presigned URL 생성
     */
    @GetMapping("/presigned-url")
    public Map<String, String> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "video/mp4") String contentType
    ) {
        String bucketKey = "test/" + fileName;
        String uploadUrl = objectStorageService.generatePresignedUploadUrl(bucketKey, contentType);
        String objectUrl = objectStorageService.getObjectUrl(bucketKey);

        return Map.of(
                "uploadUrl", uploadUrl,
                "bucketKey", bucketKey,
                "objectUrl", objectUrl
        );
    }

    @PostMapping("/stt-from-url")
    public JsonNode testSttFromUrl(@RequestBody Map<String, String> request) {
        String objectUrl = request.get("objectUrl");

        log.info("STT 테스트 시작 (URL): {}", objectUrl);

        return clovaSpeechService.recognizeSpeechFromUrl(objectUrl);
    }
}