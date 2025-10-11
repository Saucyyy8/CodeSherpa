package codeforces.codeSherpa.Config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebDriverConfig {

    @Bean(destroyMethod = "quit")
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();



        // Core anti-detection arguments
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--exclude-switches=enable-automation");
        options.addArguments("--disable-extensions-except");
        options.addArguments("--disable-plugins-discovery");
        options.addArguments("--disable-default-apps");

        // Remove automation indicators
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // User agent with latest Chrome version
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // Window and display settings
        options.addArguments("--window-size=1366,768"); // More common resolution
        options.addArguments("--start-maximized");
        options.addArguments("--disable-infobars");

        // Performance and stability
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // Language and locale
        options.addArguments("--lang=en-US");
        options.addArguments("--accept-lang=en-US,en;q=0.9");

        // Additional stealth options
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-component-extensions-with-background-pages");
        options.addArguments("--disable-ipc-flooding-protection");

        // Set preferences to mimic real user behavior
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.managed_default_content_settings.images", 1);
        prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
        prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);

        // Disable automation detection
        prefs.put("profile.default_content_setting_values.media_stream_mic", 2);
        prefs.put("profile.default_content_setting_values.media_stream_camera", 2);
        prefs.put("profile.default_content_setting_values.geolocation", 2);
        prefs.put("profile.default_content_setting_values.notifications", 2);

        options.setExperimentalOption("prefs", prefs);

        // Create ChromeDriver with options
        ChromeDriver driver = new ChromeDriver(options);

        // Execute JavaScript to remove webdriver property and other automation indicators
        driver.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        driver.executeScript("Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]})");
        driver.executeScript("Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']})");
        driver.executeScript("window.chrome = { runtime: {} }");
        driver.executeScript("Object.defineProperty(navigator, 'permissions', {get: () => ({query: () => Promise.resolve({state: 'granted'})})})");

        return driver;
    }
}