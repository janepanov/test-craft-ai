package mk.ukim.finki.testcraftai.service;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.testcraftai.model.dto.QuestionOption;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleFormsService {

    private final WebClient.Builder webClientBuilder;

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Arrays.asList(
                        "https://www.googleapis.com/auth/forms.body",
                        "https://www.googleapis.com/auth/drive"
                ));

        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    public String createFormFromQuiz(String title, Map<String, List<QuestionOption>> questionsWithOptions) throws IOException {
        String accessToken = getAccessToken();

        WebClient webClient = webClientBuilder
                .baseUrl("https://forms.googleapis.com/v1")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // Create the form
        Map<String, Object> requestBody = Map.of("info", Map.of("title", title));
        Map<String, Object> form = webClient.post()
                .uri("/forms")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        assert form != null;
        String formId = (String) form.get("formId");

        // Add questions to the form
        addQuestionsToForm(formId, questionsWithOptions, accessToken);

        // Share the form with your personal Google account
        shareFormWithUser(formId, "panovjane@gmail.com", accessToken);

        return "https://docs.google.com/forms/d/" + formId + "/edit";
    }

    private void addQuestionsToForm(String formId, Map<String, List<QuestionOption>> questionsWithOptions, String accessToken) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://forms.googleapis.com/v1/forms/" + formId + ":batchUpdate")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        List<Map<String, Object>> requests = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, List<QuestionOption>> entry : questionsWithOptions.entrySet()) {
            String questionText = entry.getKey();
            List<QuestionOption> options = entry.getValue();

            boolean isMultipleChoice = options.size() > 2;

            // Correctly declare the options as List<Map<String, Object>>
            List<Map<String, Object>> formattedOptions = options.stream()
                    .map(option -> Map.of("value", (Object) option.getText()))
                    .toList();

            Map<String, Object> questionRequest = Map.of(
                    "createItem", Map.of(
                            "item", Map.of(
                                    "title", questionText,
                                    "questionItem", Map.of(
                                            "question", Map.of(
                                                    "required", true,
                                                    "choiceQuestion", Map.of(
                                                            "type", isMultipleChoice ? "CHECKBOX" : "RADIO",
                                                            "options", formattedOptions
                                                    )
                                            )
                                    )
                            ),
                            "location", Map.of("index", index)
                    )
            );

            requests.add(questionRequest);
            index++;
        }

        Map<String, Object> payload = Map.of("requests", requests);

        // Log the payload for debugging
        System.out.println("Payload: " + payload);

        webClient.post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private void shareFormWithUser(String formId, String email, String accessToken) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://www.googleapis.com/drive/v3/files")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> permission = Map.of(
                "type", "user",
                "role", "writer",
                "emailAddress", email
        );

        webClient.post()
                .uri("/" + formId + "/permissions?sendNotificationEmail=false")
                .bodyValue(permission)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
