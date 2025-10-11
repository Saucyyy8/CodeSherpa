# 🏔️ CodeSherpa: Your AI Guide for Competitive Programming

CodeSherpa is a web application designed to help competitive programmers conquer problems from Codeforces. By providing progressive hints, solutions, and a context-aware AI chatbot, it acts as a guide, helping users learn without simply giving away the answer.

## ✨ Features

- **AI-Powered Hints**: Get multiple, progressively more revealing hints for any Codeforces problem.
- **Complete Solutions**: Access a full solution breakdown, including pseudocode, a complete code implementation, and a detailed explanation of the logic.
- **Stunning UI**: A sleek, modern, dark-themed interface designed for a great user experience.
- **Context-Aware Chatbot**: Ask follow-up questions to an AI that understands the specific problem you're working on, providing relevant and insightful guidance.
- **Web Scraping**: Automatically scrapes the problem statement, sample cases, and official editorials from Codeforces.

## ⚙️ How It Works

1.  **Enter URL**: The user pastes a URL to a Codeforces problem.
2.  **Scrape Data**: The Spring Boot backend uses **Selenium** and **Jsoup** to scrape the problem details and find the official editorial.
3.  **Generate Hints**: The scraped content is sent to the **Fireworks AI** API, which generates a series of hints, a solution, pseudocode, and an explanation based on a carefully engineered prompt.
4.  **Display Results**: The frontend, built with vanilla **HTML, CSS, and JavaScript**, displays the generated content in a clean, organized, and interactive way.
5.  **Chat with Sherpa**: The user can ask follow-up questions. The chatbot uses the context of the currently displayed problem to have an intelligent conversation, providing clarification and deeper insights.

## 🛠️ Technologies Used

- **Backend**: 
    - Java 17
    - Spring Boot 3
    - Selenium & WebDriverManager (for web scraping)
    - Jsoup (for HTML parsing)
    - Fireworks AI (for generative AI)
    - Maven (for dependency management)

- **Frontend**:
    - HTML5
    - CSS3 (with a modern, dark-theme design)
    - Vanilla JavaScript

## 🚀 Getting Started

### Prerequisites

- JDK 17 or later
- Apache Maven
- A valid API key from [Fireworks AI](https://fireworks.ai/)

### Installation & Running

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```

2.  **Configure API Key**:
    - Navigate to `src/main/resources/`.
    - Create a file named `application.properties`.
    - Add your Fireworks AI API key and the API URL to the file:
      ```properties
      fireworks.api.key=YOUR_API_KEY
      fireworks.api.url=https://api.fireworks.ai/inference/v1/chat/completions
      ```

3.  **Build and run the application**:
    ```bash
    mvn spring-boot:run
    ```

4.  **Open your browser** and go to `http://localhost:8080`.

---

This `README.md` should give visitors a great overview of the project. Let me know if you'd like any adjustments!
