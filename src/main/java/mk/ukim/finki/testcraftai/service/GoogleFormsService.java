package mk.ukim.finki.testcraftai.service;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.util.*;

@Service
public class GoogleFormsService {

    private final WebClient webClient;
    private final String token;

    public GoogleFormsService() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/forms.body"));

        credentials.refreshIfExpired();
        this.token = credentials.refreshAccessToken().getTokenValue();

        this.webClient = WebClient.builder()
                .baseUrl("https://forms.googleapis.com/v1")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String createFormFromQuiz(String title, List<String> questions) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("info", Map.of("title", title));

        Map<String, Object> form = webClient.post()
                .uri("/forms")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String formId = (String) form.get("formId");

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> item = Map.of(
                    "createItem", Map.of(
                            "item", Map.of(
                                    "title", questions.get(i),
                                    "questionItem", Map.of(
                                            "question", Map.of(
                                                    "required", true,
                                                    "textQuestion", new HashMap<>()
                                            )
                                    )
                            ),
                            "location", Map.of("index", i)
                    )
            );

            webClient.post()
                    .uri("/forms/" + formId + ":batchUpdate")
                    .bodyValue(Map.of("requests", List.of(item)))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }

        return "https://docs.google.com/forms/d/" + formId + "/edit";
    }
}
