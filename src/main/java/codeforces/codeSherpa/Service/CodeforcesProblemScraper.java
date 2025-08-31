package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.Exception.ScrapingException;
import codeforces.codeSherpa.Model.AverageProblemSpecs;
import codeforces.codeSherpa.Model.SampleTestCase;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
@Slf4j
@Service
public class CodeforcesProblemScraper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Logger logger = Logger.getLogger(CodeforcesProblemScraper.class.getName());

    @Autowired
    public CodeforcesProblemScraper(WebDriver driver){

        this.driver = driver;
        this.wait = new WebDriverWait(driver,Duration.ofSeconds(5));
    }
    public AverageProblemSpecs problemScraper(String url) throws InterruptedException{
        try{

            driver.get(url);
            Thread.sleep(2000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            AverageProblemSpecs averageProblemSpecs = new AverageProblemSpecs();
            StringBuilder contestNo = new StringBuilder();
            boolean entered = false;
            for(int i = 0;i<url.length();i++){
                while(Character.isDigit(url.charAt(i))){
                    contestNo.append(url.charAt(i));
                    i++;
                    entered = true;
                }
                if(entered) break;
            }
            averageProblemSpecs.setContestNumber(Integer.parseInt(contestNo.toString()));
            setProblemSpecs(driver,averageProblemSpecs);


            log.info("Successfully passed Scraping");
            return averageProblemSpecs;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setProblemSpecs(WebDriver driver, AverageProblemSpecs averageProblem){
        try {
            WebElement problemStatementAll = driver.findElement(By.className("problem-statement"));
            String problemName = problemStatementAll.findElement(By.className("title")).getText();
            if(problemName.isEmpty()){
                //log error
                log.error("THE PROBLEM STATEMENT NOT FOUND");
                throw new ScrapingException("PROBLEM STATEMENT NOT FOUND");
            }
            averageProblem.setProblemChar(problemName.charAt(0));
            averageProblem.setProblemName(problemName);
            String timeLimit = problemStatementAll.findElement(By.className("time-limit")).getText().replaceAll("time limit per test"," ");
            if(timeLimit.isEmpty()){
                log.error("TIME LIMIT NOT FOUND");
            }
            averageProblem.setTimeLimit(timeLimit);
            try{
                String memoryLimit = problemStatementAll.findElement(By.className("memory-limit")).getText().replaceAll("memory limit per test"," ");
                averageProblem.setMemoryLimit(memoryLimit);
            }
            catch (Exception e){
                log.error("MEMORY LIMIT NOT FOUND");
            }
            try{
                List<WebElement> problemTextElements = problemStatementAll.findElements(By.tagName("p"));

                String problemText = problemTextElements.stream()
                        .map(WebElement::getText)
                        .collect(Collectors.joining("\n"));


                problemText = problemText.replaceAll("\n"," ");

                problemText = formatMathString(problemText);
                averageProblem.setProblemStatement(problemText);
            }
            catch (ScrapingException e){
                throw new ScrapingException("NO PROBLEM STATEMENT FOUND");
            }

            try{
                String inputSpecs = problemStatementAll.findElement(By.className("input-specification")).getText().replaceAll("\n"," ");
                averageProblem.setInputSpecs(inputSpecs);
            }
            catch (NoSuchElementException exception){
                log.error("No input Specification Found");
            }

            try{
                String outputSpecs = problemStatementAll.findElement(By.className("output-specification")).getText().replaceAll("\n"," ");
                averageProblem.setOutputSpecs(outputSpecs);
            }
            catch (NoSuchElementException exception){
                log.error("No output specification, probabaly an interactive problem");
                try{
                    String outputSpecs = problemStatementAll.findElement(By.className("section-title")).getText().replaceAll("\n"," ");
                    averageProblem.setOutputSpecs(outputSpecs);
                } catch (Exception e) {
                    log.error("Couldn't find any output specification");
                }
            }



            String pageTitle = driver.getTitle();
            if(pageTitle==null){
                throw new ScrapingException("Page title not found..ERRURR");
            }

            try{
                List<WebElement> sampleInputs = problemStatementAll.findElements(By.cssSelector(".sample-test .input pre"));
                List<WebElement> sampleOutputs = problemStatementAll.findElements(By.cssSelector(".sample-test .output pre"));
                averageProblem.setTestCases(new ArrayList<>());
                for (int i = 0; i < sampleInputs.size(); i++) {
                    String inputData = sampleInputs.get(i).getText();
                    String outputData = sampleOutputs.get(i).getText();
                    if(inputData.isEmpty() || outputData.isEmpty()){
                        log.error("INPUT || OUTPUT SAMPLE TESTCASE NOT FOUND");
                    }
                    averageProblem.getTestCases().add(new SampleTestCase(inputData, outputData));
                }
            }
            catch (Exception e){
                log.error("NO SAMPLE INPUT | OUTPUT ELEMENTS FOUND");
            }

        }
        catch (ScrapingException se){
            throw new ScrapingException("PROBLEM TEXT NOT FOUND");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> editorialSolutionScraper(String url,int probNo, String ch) {
        try{
            driver.get(url);
            System.out.println(probNo+" "+ch);
            System.out.println(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement tutorial = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(text(),'Tutorial')]")
            ));

            String link = tutorial.getAttribute("href");
            System.out.println("Link to tutorial is "+link);

            List<String> result = scrapeProblem(driver,link,probNo,ch);
            System.out.println(result);
            return result;

        }
        catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new ScrapingException("Error in Tutorial part of the code..");

        }
    }

    public static List<String> scrapeProblem(WebDriver driver, String url,
                                                       int problemNumber, String problemLetter) {
        System.out.println("Problem Number is: "+problemNumber);
        List<String> items = new ArrayList<>();
        String targetHref = "/contest/" + problemNumber + "/problem/" + problemLetter;
        String problemHrefPrefix = "/contest/" + problemNumber + "/problem/";

        try {
            driver.get(url);
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            Element problemHeader = doc.selectFirst("a[href='" + targetHref + "']");
            if (problemHeader == null) {
                System.out.println("Could not find problem header for " + problemNumber + problemLetter);
                return items;
            }

            // start searching siblings from the header's parent
            Element currentElement = problemHeader.parent();

            while ((currentElement = currentElement.nextElementSibling()) != null) {

                // STOP if we encounter any link that looks like another problem for the same contest
                Element nextProblemAnchor = currentElement.selectFirst("a[href^=\"" + problemHrefPrefix + "\"]");
                if (nextProblemAnchor != null) {
                    String href = nextProblemAnchor.attr("href");
                    if (!href.equals(targetHref)) {
                        System.out.println("Reached a different problem (" + href + "). Stop searching.");
                        break;
                    }
                }

                // handle spoiler blocks
                if (currentElement.hasClass("spoiler")) {
                    Element spoilerTitle = currentElement.selectFirst(".spoiler-title");
                    String titleText = spoilerTitle != null ? spoilerTitle.text().toLowerCase() : "";

                    Element content = currentElement.selectFirst(".spoiler-content");
                    if (content == null) continue;

                    // 1) extract math (MathJax script) first and remove them to avoid duplication
                    for (Element s : content.select("script[type=math/tex]")) {
                        String math = s.text().trim();
                        if (!math.isEmpty()) items.add("MATH: " + math);
                        s.remove();
                    }


                    // 3) remaining cleaned text
                    String cleaned = Jsoup.parse(content.html()).text().trim();
                    String label;
                    if (titleText.contains("hint")) label = "HINT";
                    else if (titleText.contains("editorial")) label = "EDITORIAL";
                    else if (titleText.contains("tutorial")) label = "TUTORIAL";
                    else if (titleText.contains("code")) label = "CODE";
                    else if (titleText.contains("solution") || titleText.equalsIgnoreCase("solution")) label = "SOLUTION";
                    else label = "OTHER";

                    if (!cleaned.isEmpty()) items.add(label + ": " + cleaned);

                    // if we just captured a solution, stop (you asked to capture all occurrences but stop after solution)
                    if ("CODE".equals(label)) {
                        System.out.println("Found Solution — finishing collection.");
                        break;
                    }

                    // continue scanning for more spoilers belonging to the same problem
                    continue;
                }

                // (Optional) if you want to capture non-spoiler textual blocks too:
                // You could add logic here to capture plain paragraphs until next problem.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public static String formatMathString(String rawText) {
        String cleanedText = rawText;

        // 1. Replace LaTeX commands like \le, \ge, \cdot
        cleanedText = cleanedText.replaceAll("\\\\le", "<=");
        cleanedText = cleanedText.replaceAll("\\\\ge", ">=");
        cleanedText = cleanedText.replaceAll("\\\\cdot", "*");

        // 2. Remove subscripts like "x_i" -> "xi"
        // This finds a letter/number, an underscore, and another letter/number
        cleanedText = cleanedText.replaceAll("([a-zA-Z0-9])_([a-zA-Z0-9])", "$1$2");

        // 3. Remove the triple dollar signs around variables like "$$$n$$$" -> "n"
        // The (.*?) part captures the text between the dollar signs
        cleanedText = cleanedText.replaceAll("\\$\\$\\$(.*?)\\$\\$\\$", "$1");

        // 4. (Optional) Clean up any extra spaces that might be left over
        cleanedText = cleanedText.replaceAll("\\s+", " ").trim();
        return cleanedText;

    }
}
