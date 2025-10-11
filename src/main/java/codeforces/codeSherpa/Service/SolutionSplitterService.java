package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.DTO.HintOutputDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SolutionSplitterService {

    // canonical keys we recognize (lowercase, without trailing colon)
    private static final List<String> KEYS = Arrays.asList(
            "hint 1", "hint 2", "hint 3",
            "solution to problem", "pseudocode", "code",
            "explanation of the code"
    );

    public HintOutputDto parse(String rawText) {
        HintOutputDto dto = new HintOutputDto();

        if (rawText == null || rawText.isEmpty()) {
            return dto;
        }

        // Map from canonical key -> accumulated text
        Map<String, StringBuilder> sections = new LinkedHashMap<>();
        for (String k : KEYS) sections.put(k, new StringBuilder());

        String currentKey = null;           // which section we are writing into
        boolean inCodeBlock = false;        // toggled by ```
        String[] lines = rawText.split("\n", -1);

        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine;

            // Detect code fence start/end (triple backticks).
            // We treat any line that starts with ``` as a fence toggle.
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("```")) {
                // toggle code-block state and append the fence line as well
                inCodeBlock = !inCodeBlock;
                // ensure we have a current section to keep code in; default to pseudocode if unknown
                if (currentKey == null) {
                    currentKey = "pseudocode";
                }
                sections.get(currentKey).append(line).append("\n");
                continue;
            }

            // If we are inside a code block, just append (do NOT treat headings inside code)
            if (inCodeBlock) {
                if (currentKey == null) currentKey = "pseudocode";
                sections.get(currentKey).append(line).append("\n");
                continue;
            }

            // Not in code block: check if the line *starts* with a recognized heading.
            // Accept optional leading # marks (markdown headings) then check startsWith key.
            String candidate = trimmedLine;
            // remove leading '#'s and spaces if present
            while (candidate.startsWith("#")) {
                candidate = candidate.substring(1).trim();
            }
            String candidateLower = candidate.toLowerCase(Locale.ROOT);

            boolean matchedHeading = false;
            for (String key : KEYS) {
                // allow heading with or without colon, e.g. "Hint 1:" or "Hint 1"
                if (candidateLower.startsWith(key + ":") || candidateLower.equals(key) || candidateLower.startsWith(key + " ")) {
                    currentKey = key;
                    matchedHeading = true;
                    break;
                }
            }

            if (matchedHeading) {
                // Found a heading line: do NOT add this heading line to the content.
                // Continue to next line so content begins after heading.
                continue;
            }

            // If no heading matched, append to current section if any; otherwise put into solution-to-problem fallback.
            if (currentKey == null) {
                // if nothing designated yet, prefer 'solution to problem' as the fallback container
                currentKey = "solution to problem";
            }
            sections.get(currentKey).append(line).append("\n");
        }

        // Assign accumulated text to DTO (clean code blocks for pseudocode/code)
        dto.setHint1(trimOrNull(sections.get("hint 1")));
        dto.setHint2(trimOrNull(sections.get("hint 2")));
        dto.setHint3(trimOrNull(sections.get("hint 3")));
        dto.setSolutionToProblem(trimOrNull(sections.get("solution to problem")));
        dto.setPseudocode(cleanCodeBlock(trimOrNull(sections.get("pseudocode"))));
        dto.setCode(cleanCodeBlock(trimOrNull(sections.get("code"))));
        dto.setExplanationOfCode(trimOrNull(sections.get("explanation of the code")));

        return dto;
    }

    // remove leading/trailing whitespace or return null if empty
    private String trimOrNull(StringBuilder sb) {
        if (sb == null) return null;
        String s = sb.toString().trim();
        return s.isEmpty() ? null : s;
    }

    // Remove triple-backtick fences if present and trim
    private String cleanCodeBlock(String codeBlock) {
        if (codeBlock == null) return null;
        String cleaned = codeBlock.trim();
        // remove leading ```lang or ```
        cleaned = cleaned.replaceAll("(?m)^```\\w*\\s*", "");
        // remove trailing ```
        cleaned = cleaned.replaceAll("(?m)\\s*```\\s*$", "");
        return cleaned.trim();
    }
}
