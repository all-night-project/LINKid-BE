package com.example.linkid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ClovaSpeechService {

    @Value("${ncloud.clova-speech.secret-key}")
    private String secretKey;

    @Value("${ncloud.clova-speech.invoke-url}")
    private String invokeUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode recognizeSpeechFromUrl(String mediaUrl) {
        try {
            Map<String, Object> requestBody = getStringObjectMap(mediaUrl);

            String response = webClient.post()
                    .uri(invokeUrl + "/recognizer/url")
                    .header("X-CLOVASPEECH-API-KEY", secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Clova Speech 응답 (URL): {}", response);

            return objectMapper.readTree(response);

        } catch (Exception e) {
            log.error("Clova Speech URL API 호출 실패", e);

            if (e instanceof WebClientResponseException) {
                WebClientResponseException ex = (WebClientResponseException) e;
                String errorBody = ex.getResponseBodyAsString();
                log.error("Clova API 오류 응답 본문: {}", errorBody);
                // 런타임 예외를 던질 때 Clova의 원본 오류 메시지를 포함
                throw new RuntimeException("STT 처리 실패: " + errorBody, e);
            }

            throw new RuntimeException("STT 처리 실패: " + e.getMessage());
        }
    }

    private static Map<String, Object> getStringObjectMap(String mediaUrl) {
        Map<String, Object> params = Map.of(
                "language", "ko-KR",
                "completion", "sync",
                "wordAlignment", false,
                "fullText", false,
                "diarization", Map.of(
                        "enable", true,
                        "speakerCountMin", 2,
                        "speakerCountMax", 2
                )
        );

        Map<String, Object> requestBody = new HashMap<>(params);

        requestBody.put("url", mediaUrl);
        return requestBody;
    }
}