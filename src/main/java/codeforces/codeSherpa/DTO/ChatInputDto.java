package codeforces.codeSherpa.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatInputDto {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    // This will hold the entire context (hints, solution, etc.)
    private HintOutputDto problemContext;

    // This will hold the specific hint/code the user is currently looking at
    private String currentHint;
}
