package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.Config.PromptLoader;
import codeforces.codeSherpa.DTO.FireworksApiResponse;
import codeforces.codeSherpa.DTO.HintOutputDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final WebClient webClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper; // For converting context to JSON

    @Value("${fireworks.api.key}")
    private String apiKey;

    @Autowired
    public AiService(WebClient.Builder webClientBuilder, @Value("${fireworks.api.url}") String apiUrl, PromptLoader promptLoader, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public String getChatCompletion(String solution, String problemStatement) {
        String promptStatic;
        try {
            promptStatic = promptLoader.loadPromptFile("GiveHints.txt");
        } catch (IOException e) {
            logger.error("Failed to load prompt file: GiveHints.txt", e);
            throw new RuntimeException(e);
        }

        promptStatic = promptStatic.replace("{{my_problem_statement}}", problemStatement);
        promptStatic = promptStatic.replace("{{my_solution}}", solution);

        return callFireworksApi(promptStatic);
    }

    public String getChatReply(String message, HintOutputDto problemContext, String currentHint) {
        String promptStatic;
        try {
            promptStatic = promptLoader.loadPromptFile("ChatbotPrompt.txt");
        } catch (IOException e) {
            logger.error("Failed to load prompt file: ChatbotPrompt.txt", e);
            throw new RuntimeException(e);
        }

        String contextJson = "";
        try {
            // Convert the context object to a JSON string for clear prompting
            contextJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(problemContext);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize problem context to JSON", e);
            // Continue with an empty context if serialization fails
        }

        promptStatic = promptStatic.replace("{{user_message}}", message);
        promptStatic = promptStatic.replace("{{problem_context}}", contextJson);
        promptStatic = promptStatic.replace("{{current_hint}}", currentHint != null ? currentHint : "None");

        return callFireworksApi(promptStatic);
    }

    private String callFireworksApi(String prompt) {
        logger.info("Final prompt being sent to API (first 200 chars): {}", prompt.substring(0, Math.min(prompt.length(), 200)));

        Map<String, Object> requestBody = Map.of(
                "model", "accounts/fireworks/models/deepseek-v3p1",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            logger.info("Sending request to Fireworks AI...");
            FireworksApiResponse apiResponse = webClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(FireworksApiResponse.class)
                    .block(Duration.ofSeconds(90)); // Increased timeout for potentially longer context

            if (apiResponse != null && apiResponse.getChoices() != null && !apiResponse.getChoices().isEmpty()) {
                String content = apiResponse.getChoices().getFirst().getMessage().getContent();
                logger.info("Successfully extracted content from response.");
                return content;
            } else {
                logger.error("Response was empty or malformed.");
                return "Error: Could not parse a valid response from the API.";
            }
        } catch (Exception e) {
            logger.error("The API call failed or timed out.", e);
            throw new RuntimeException("API call failed to complete.", e);
        }
    }
}
