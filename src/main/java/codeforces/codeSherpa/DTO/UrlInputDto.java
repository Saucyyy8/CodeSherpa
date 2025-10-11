package codeforces.codeSherpa.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlInputDto {

    @NotNull(message = "Input should not be Empty")
    @Pattern(
            regexp = "https://codeforces.com.*",
            message = "Should be a Valid Codeforces Problem Link"
    )
    private String url;
}
