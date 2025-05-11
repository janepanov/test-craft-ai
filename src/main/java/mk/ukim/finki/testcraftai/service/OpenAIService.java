package mk.ukim.finki.testcraftai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mk.ukim.finki.testcraftai.model.dto.QuizGenerationRequest;
import mk.ukim.finki.testcraftai.model.dto.QuizGenerationResponse;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAIService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a quiz based on the provided text content
     *
     * @param content the educational content to generate questions from
     * @param request parameters for quiz generation
     * @return response containing generated quiz questions
     * @throws JsonProcessingException if JSON processing fails
     */
    public QuizGenerationResponse generateQuiz(String content, QuizGenerationRequest request)
            throws JsonProcessingException {
        String promptText = buildQuizGenerationPrompt(content, request);

        Prompt prompt = new Prompt(List.of(new UserMessage(promptText)));
        ChatResponse response = chatClient.call(prompt);
        String responseContent = response.getResult().getOutput().getContent();

        return objectMapper.readValue(responseContent, QuizGenerationResponse.class);
    }

    /**
     * Builds the prompt for quiz generation
     *
     * @param content the educational content
     * @param request parameters for quiz generation
     * @return formatted prompt text
     */
    private String buildQuizGenerationPrompt(String content, QuizGenerationRequest request) {
        String questionTypes = request.getQuestionTypes().stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(type -> type.replace("_", " "))
                .reduce((a, b) -> a + " and " + b)
                .orElse("questions");

        return String.format(
                """
                        You are an expert educational content creator specializing in creating quiz questions.
                        
                        I will provide you with educational content, and I want you to generate %d quiz questions based on this content.
                        The quiz should focus on the most important concepts and information in the material.
                        
                        For each question:
                        - Create %s type questions only
                        - Ensure questions test understanding, not just memorization
                        - Make sure all questions are directly based on the provided content
                        - For multiple-choice questions, provide 4 options with exactly one correct answer
                        
                        Return a JSON in this format:
                        {
                          "title": "An appropriate title for this quiz based on the content",
                          "questions": [
                            {
                              "questionText": "Question text here",
                              "type": "MULTIPLE_CHOICE",
                              "options": [
                                {"optionText": "Option 1", "isCorrect": false},
                                {"optionText": "Option 2", "isCorrect": true},
                                {"optionText": "Option 3", "isCorrect": false},
                                {"optionText": "Option 4", "isCorrect": false}
                              ]
                            },
                            {
                              "questionText": "True/False question text here",
                              "type": "TRUE_FALSE",
                              "options": [
                                {"optionText": "True", "isCorrect": true},
                                {"optionText": "False", "isCorrect": false}
                              ]
                            }
                          ]
                        }
                        
                        Educational content:
                        "%s\"""",
                request.getNumberOfQuestions(),
                questionTypes,
                content
        );
    }
}
