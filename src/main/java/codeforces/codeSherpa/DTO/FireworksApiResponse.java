package codeforces.codeSherpa.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// This tells the parser to ignore any extra fields in the JSON
@JsonIgnoreProperties(ignoreUnknown = true)
public class FireworksApiResponse {

    private List<Choice> choices;

    // Getters and Setters
    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}