package codeforces.codeSherpa.Service;

// Make sure to import your new DTO classes

import codeforces.codeSherpa.Config.PromptLoader;
import codeforces.codeSherpa.DTO.FireworksApiResponse;
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

    @Value("${fireworks.api.key}")
    private String apiKey;

    @Autowired
    public AiService(WebClient.Builder webClientBuilder, @Value("${fireworks.api.url}") String apiUrl, PromptLoader promptLoader) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.promptLoader = promptLoader;
    }

    public String getChatCompletion(String prompt) {
        String promptStatic = null;
        try {
            promptStatic = promptLoader.loadPromptFile("GiveHints.txt");
        } catch (IOException e) {
            logger.error("Failed to load prompt file", e);
            throw new RuntimeException(e);
        }

        promptStatic = promptStatic.replace("my_problem_statement", prompt);
        logger.info("Final prompt being sent to API: {}", promptStatic);

        Map<String, Object> requestBody = Map.of(
                "model", "accounts/fireworks/models/deepseek-v3p1", // I see you changed the model, that's fine
                "messages", List.of(
                        Map.of("role", "user", "content", promptStatic)
                )
        );

        try {
            logger.info("Sending request to Fireworks AI...");
            // --- START OF CHANGES ---

            // 1. Tell WebClient to expect our new DTO class instead of a String
            FireworksApiResponse apiResponse = webClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(FireworksApiResponse.class) // Changed from String.class
                    .block(Duration.ofSeconds(60));

            // 2. Safely get the content from the parsed object
            if (apiResponse != null && apiResponse.getChoices() != null && !apiResponse.getChoices().isEmpty()) {
                String content = apiResponse.getChoices().getFirst().getMessage().getContent();
                logger.info("Successfully extracted content from response.");
                // This will now return JUST the hint text
                return content;
            } else {
                logger.error("Response was empty or malformed.");
                return "Error: Could not parse a valid response from the API.";
            }

            // --- END OF CHANGES ---
        } catch (Exception e) {
            logger.error("The .block() call failed or timed out.", e);
            throw new RuntimeException("API call failed to complete.", e);
        }
    }
}