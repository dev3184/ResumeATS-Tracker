package com.resumeats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.1";

    public String analyzeResume(String resumeText) {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        String prompt = """
        You are a professional resume evaluator AI.

        Analyze the following resume and return ONLY in the following format:

        ---
        Score: <score>/100

        Suggestions:
        - Improvement: <resume formatting / content improvement>
        - Grammar: <grammar/spelling/language correction if any>
        - Job Suggestion: <job roles suitable for this candidate>
        ---

        Do not repeat the resume content. Only respond in this structured format.

        Resume:


        """ + resumeText;

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(new HuggingFaceRequest(prompt));
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error preparing HuggingFace request.";
        }

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(HF_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("🧠 HuggingFace Raw Response:\n" + responseBody);

            if (!response.isSuccessful()) {
                return "❌ HuggingFace API error: " + response.code() + " - " + response.message();
            }

            JsonNode json = objectMapper.readTree(responseBody);
            if (json.isArray() && json.size() > 0 && json.get(0).has("generated_text")) {
                return json.get(0).get("generated_text").asText();
            } else {
                return "⚠️ Unexpected response format from HuggingFace.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Error calling HuggingFace API: " + e.getMessage();
        }
    }

    static class HuggingFaceRequest {
        public String inputs;

        HuggingFaceRequest(String inputs) {
            this.inputs = inputs;
        }
    }
}
