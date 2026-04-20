package com.assignment.service;

import com.assignment.model.RegistrationRequest;
import com.assignment.model.RegistrationResponse;
import com.assignment.model.SubmissionRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public RegistrationResponse generateWebhook(String name, String regNo, String email) {
        RegistrationRequest request = new RegistrationRequest(name, regNo, email);
        return restTemplate.postForObject(GENERATE_URL, request, RegistrationResponse.class);
    }

    public void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        SubmissionRequest request = new SubmissionRequest(finalQuery);
        HttpEntity<SubmissionRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.postForObject(webhookUrl, entity, String.class);
    }
}
