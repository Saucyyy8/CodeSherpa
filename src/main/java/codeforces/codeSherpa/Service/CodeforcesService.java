package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.DTO.ChatInputDto;
import codeforces.codeSherpa.DTO.HintOutputDto;
import codeforces.codeSherpa.Model.AverageProblemSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeforcesService {

    private final CodeforcesProblemScraper scraper;
    private final AiService aiService;
    private final SolutionSplitterService solutionParser;

    @Autowired
    public CodeforcesService(CodeforcesProblemScraper scraper, AiService service, SolutionSplitterService solutionParser) {
        this.aiService = service;
        this.scraper = scraper;
        this.solutionParser = solutionParser;
    }

    public HintOutputDto getHint(String url) throws InterruptedException {
        AverageProblemSpecs problem = scraper.problemScraper(url);
        List<String> s = scraper.editorialSolutionScraper(url, problem);

        StringBuilder allProblemSoln = new StringBuilder();
        for (String i : s) allProblemSoln.append(i).append("\n");

        StringBuilder problemStatement = new StringBuilder();
        problemStatement.append("Problem Statement: ").append(problem.getProblemStatement()).append("\n");
        problemStatement.append("Input Specs: ").append(problem.getInputSpecs()).append("\n");
        problemStatement.append("Output Specs: ").append(problem.getOutputSpecs()).append("\n");
        problemStatement.append("Sample Cases: ").append(problem.getTestCases());

        String responseHints = aiService.getChatCompletion(allProblemSoln.toString(), problemStatement.toString());
        HintOutputDto hints = solutionParser.parse(responseHints);

        // Also attach the original problem statement to the context for the chatbot
        hints.setSolutionToProblem(problem.getProblemStatement() + "\n\n" + hints.getSolutionToProblem());

        return hints;
    }

    /**
     * Gets a conversational reply from the AI service, aware of the problem context.
     * @param chatInput The user's message and the current problem context.
     * @return The AI's reply.
     */
    public String getChatReply(ChatInputDto chatInput) {
        // Pass the user's message and the full context to the AI service
        return aiService.getChatReply(chatInput.getMessage(), chatInput.getProblemContext(), chatInput.getCurrentHint());
    }
}
