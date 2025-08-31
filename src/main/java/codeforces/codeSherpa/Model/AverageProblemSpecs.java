package codeforces.codeSherpa.Model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AverageProblemSpecs {

    private String url;
    private String problemName;

    private String timeLimit;
    private String memoryLimit;
    private String problemStatement;
    private String inputSpecs;
    private String outputSpecs;

    private List<SampleTestCase> testCases;
    private int contestNumber;
    private char problemChar;
}
