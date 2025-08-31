package codeforces.codeSherpa.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HintOutputDto {
    private String hint1;
    private String hint2;
    private String hint3;
    private String solutionToProblem;
    private String pseudocode;
    private String code;
    private String explanationOfCode;
}
