package codeforces.codeSherpa.Config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    // visible profile name shown by Chrome UI (e.g. "Project" or "Suhas")
    @Bean(destroyMethod = "quit")
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Headless mode
        options.addArguments("--headless=new");

        // Anti-detection measures
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // User agent
        options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        // Window size
        options.addArguments("--window-size=1920,1080");

        // Additional preferences
        options.addArguments("--disable-blink-features");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Experimental options
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // Additional stability options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");

        return new ChromeDriver(options);
    }
}
