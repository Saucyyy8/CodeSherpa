package codeforces.codeSherpa.Service;

import codeforces.codeSherpa.Exception.ScrapingException;
import codeforces.codeSherpa.Model.AverageProblemSpecs;
import codeforces.codeSherpa.Model.SampleTestCase;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
@Service
public class CodeforcesProblemScraper {
    private static WebDriver driver;
    private final WebDriverWait wait;
    private final Logger logger = Logger.getLogger(CodeforcesProblemScraper.class.getName());

    @Autowired
    public CodeforcesProblemScraper(WebDriver driver){

        this.driver = driver;
        this.wait = new WebDriverWait(driver,Duration.ofSeconds(5));
    }
    public AverageProblemSpecs problemScraper(String url) throws InterruptedException{
        try{
            System.out.println("problemScraper Method");
            driver.get(url);
            System.out.println("About to wait");
            Thread.sleep(2000);
            System.out.println("Almost done waiting");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            System.out.println("Done Waiting");
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
            System.out.println(averageProblem);

        }
        catch (ScrapingException se){
            throw new ScrapingException("PROBLEM TEXT NOT FOUND");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> editorialSolutionScraper(String url, AverageProblemSpecs problem) {
        try {
            driver.get(url);

            System.out.println(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // wait for ANY tutorial anchors to be present (if none present this will time out)
            List<WebElement> tutorials = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.xpath("//a[contains(translate(., 'TUTORIAL', 'tutorial'), 'tutorial')]")
                    )
            );

            // debug: print what we found
            System.out.println("Found tutorial anchors: " + tutorials.size());
            for (WebElement t : tutorials) {
                try {
                    System.out.println(" -> text: [" + t.getText().trim() + "], href: " + t.getAttribute("href") + ", displayed: " + t.isDisplayed());
                } catch (StaleElementReferenceException sere) {
                    System.out.println(" -> stale element while printing");
                }
            }

            // Choose best candidate
            WebElement best = null;
            Pattern p = Pattern.compile("#\\s*(\\d+)", Pattern.CASE_INSENSITIVE); // matches "Tutorial #3"
            int bestNum = -1;
            boolean bestIsEn = false;

            for (WebElement t : tutorials) {
                try {
                    if (!t.isDisplayed()) continue; // prefer visible links
                } catch (StaleElementReferenceException sere) {
                    continue;
                }
                String text = t.getText() == null ? "" : t.getText().trim();
                String href = t.getAttribute("href") == null ? "" : t.getAttribute("href");

                // prefer links that contain 'tutorial' in href (helps ignore unrelated 'Tutorial' matches)
                if (!href.toLowerCase().contains("tutorial") && !text.toLowerCase().contains("tutorial")) {
                    // still consider them but lower priority -- skip for now
                    // continue;
                }

                Matcher m = p.matcher(text);
                if (m.find()) {
                    int num = Integer.parseInt(m.group(1));
                    boolean isEn = text.toLowerCase().contains("(en)");
                    if (num > bestNum || (num == bestNum && isEn && !bestIsEn)) {
                        best = t;
                        bestNum = num;
                        bestIsEn = isEn;
                    }
                } else {
                    // no number, but if we don't have any numbered candidate yet, consider it as fallback
                    if (best == null) {
                        best = t;
                        bestNum = -1;
                        bestIsEn = text.toLowerCase().contains("(en)");
                    } else {
                        // if both non-numbered, prefer one with (en)
                        if (!bestIsEn && text.toLowerCase().contains("(en)")) {
                            best = t;
                            bestIsEn = true;
                        }
                    }
                }
            }

            // fallback: if no visible candidate, take last element in original list
            if (best == null && !tutorials.isEmpty()) {
                best = tutorials.get(tutorials.size() - 1);
            }

            if (best == null) {
                throw new ScrapingException("No tutorial links found on page.");
            }

            String link = best.getAttribute("href");
            System.out.println("Chosen tutorial link: " + link + " (text: " + best.getText() + ")");

            List<String> result = scrapeEditorial(link, problem);
            System.out.println(result);
            return result;

        } catch (TimeoutException te) {
            System.out.println("Timeout waiting for tutorial links: " + te.getMessage());
            throw new ScrapingException("No tutorial links found (timeout).");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ScrapingException("Error in getting the tutorial / editorial link to the code.");
        }
    }
    public static List<String> scrapeEditorial(String url, AverageProblemSpecs problem) {
        List<String> items = new ArrayList<>();
        String problemName = problem.getProblemName().substring(problem.getProblemName().indexOf('.') + 1).trim();

        System.out.println("Scraping editorial for problem: " + problemName);

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("ttypography")));

            WebElement contentArea = driver.findElement(By.className("ttypography"));


            // Find the starting element (h1, h2, h3, h4, p) that contains the problem name
            List<WebElement> potentialHeaders = contentArea.findElements(By.xpath(".//h1 | .//h2 | .//h3 | .//h4 | .//p"));
            WebElement startElement = null;
            for (WebElement header : potentialHeaders) {
                if (header.getText().contains(problemName)) {
                    startElement = header;
                    System.out.println("Found starting element for " + problemName + ": " + startElement.getTagName());
                    break;
                }
            }

            if (startElement == null) {
                System.err.println("Could not find the starting element for problem: " + problemName);
                return items;
            }

            // Get all subsequent siblings
            List<WebElement> subsequentSiblings = startElement.findElements(By.xpath("following-sibling::*"));

            boolean scrapingStarted = false;

            for (WebElement sibling : subsequentSiblings) {
                String tagName = sibling.getTagName().toLowerCase();
                String elementClass = sibling.getAttribute("class");

                // If we encounter a spoiler, start scraping
                if (elementClass != null && elementClass.toLowerCase().contains("spoiler")) {
                    scrapingStarted = true;
                    try {
                        WebElement contentEl = sibling.findElement(By.className("spoiler-content"));
                        String innerHtml = contentEl.getAttribute("innerHTML");
                        if (innerHtml != null && !innerHtml.trim().isEmpty()) {
                            Document contentDoc = Jsoup.parse(innerHtml);
                            String cleanedText = contentDoc.text().trim();
                            if (!cleanedText.isEmpty()) {
                                items.add("SOLUTION: " + cleanedText);
                                System.out.println("Added spoiler content: " + cleanedText.substring(0, Math.min(100, cleanedText.length())) + "...");
                            }
                        }
                    } catch (NoSuchElementException e) {
                        System.out.println("Spoiler content not found, skipping.");
                    }
                } else if (scrapingStarted && tagName.equals("p")) {
                    // If we have started scraping and we encounter a <p> tag, stop.
                    System.out.println("Encountered a <p> tag after scraping started. Stopping.");
                    break;
                } else if (scrapingStarted && (tagName.startsWith("h") || tagName.equals("p"))) {
                    // also break if we find a new problem header
                    if(sibling.getText().matches("^[A-Z]\\.\\s.*$")){
                        System.out.println("Encountered a new problem header. Stopping.");
                        break;
                    }

                }
            }

            System.out.println("Scraping finished. Final items count: " + items.size());

        } catch (Exception e) {
            System.err.println("An error occurred during editorial scraping: " + e.getMessage());
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
