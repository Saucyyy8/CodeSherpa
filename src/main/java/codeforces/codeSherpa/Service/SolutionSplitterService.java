package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.DTO.HintOutputDto;
import org.springframework.stereotype.Service;

@Service
public class SolutionSplitterService {

    public HintOutputDto parse(String rawText) {
        HintOutputDto solution = new HintOutputDto();

        // 1. Define a regex pattern that includes all your keywords as "OR" conditions.
        // The keywords are the delimiters we will split the string by.
        String delimiterRegex = "Hint 1:|Hint 2:|Hint 3:|Solution to Problem:|Pseudocode:|Code:|Explanation of the Code:";

        // 2. Split the raw text into an array based on the keywords.
        // The first element of the array will be empty because the text starts with a keyword.
        String[] parts = rawText.split(delimiterRegex);

        // 3. Assign the parts to the object's fields, trimming whitespace.
        // We start from index 1 because parts[0] is empty.
        if (parts.length > 1) solution.setHint1(parts[1].trim());
        if (parts.length > 2) solution.setHint2(parts[2].trim());
        if (parts.length > 3) solution.setHint3(parts[3].trim());
        if (parts.length > 4) solution.setSolutionToProblem(parts[4].trim());
        if (parts.length > 5) solution.setPseudocode(cleanCodeBlock(parts[5]));
        if (parts.length > 6) solution.setCode(cleanCodeBlock(parts[6]));
        if (parts.length > 7) solution.setExplanationOfCode(parts[7].trim());

        return solution;
    }

    // Helper method to remove the ``` markers from code blocks
    private String cleanCodeBlock(String codeBlock) {
        String cleaned = codeBlock.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        // Also remove the language hint like 'cpp' if it exists
        if (cleaned.trim().startsWith("cpp")) {
            cleaned = cleaned.trim().substring(3);
        }
        return cleaned.trim();
    }
}