package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.DTO.HintOutputDto;
import codeforces.codeSherpa.Model.AverageProblemSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeforcesService {

    private final CodeforcesProblemScraper scraper;
    private final AiService aiService;
    private SolutionSplitterService solutionParser;

    @Autowired
    public CodeforcesService(CodeforcesProblemScraper scraper, AiService service,SolutionSplitterService solutionParser){

        this.aiService = service;this.scraper = scraper; this.solutionParser = solutionParser;
    }

    public HintOutputDto getHint(String url) throws InterruptedException {
        //login

        AverageProblemSpecs problem = scraper.problemScraper(url);


        List<String> s = scraper.editorialSolutionScraper(url, problem.getContestNumber(), String.valueOf(problem.getProblemChar()));
        StringBuilder allProblemStatement = new StringBuilder();
        for(String i : s) allProblemStatement.append(i).append("/n");

        String responseHints = aiService.getChatCompletion(allProblemStatement.toString());
        System.out.println("Got back Something to Print");
        System.out.println(responseHints);

        HintOutputDto hints = solutionParser.parse(responseHints);
        System.out.println(hints);


        return hints;
    }


}
