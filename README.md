# MentiBot App

A desktop application for Mentimeter quizzes that fetches and parses all quiz questions _before_ the session starts, displaying them in a simple Swing-based UI and logging timing information for each step.

## Description

MentiBot UI connects to a Mentimeter slide deck by its participation code, loads the page headlessly (using HtmlUnitDriver), waits for the quiz data to become available in the page’s JavaScript context, extracts all questions and choices via Jackson JSON parsing, and presents them in a scrollable Swing window. Execution times for each stage are printed to the console for performance monitoring.

## Features

- **Fetch participation key** from slide-deck ID  
- **Headless page load** with HtmlUnitDriver & built-in JavaScript support  
- **Custom readiness check** on `window.__next_f` array  
- **JSON extraction & parsing** of questions and choices  
- **Swing UI** for entering deck ID and viewing results  
- **Console timing logs** for each major step  
- **Graceful error handling** and UI feedback  

## Prerequisites

- Java 8 or higher  
- Maven or Gradle build tool  
- Internet connection (to reach Mentimeter servers)  

## Installation

1. **Clone the repository**  
   ```bash
   git clone https://github.com/Corovnius/mentiBot.git
   cd mentibot
   ```

2. **Build with Maven**  
   ```bash
   mvn clean package
   ```

3. **Run the JAR**  
   ```bash
   java -jar target/mentibot-ui-1.0.0.jar
   ```

## Configuration

- **Timeouts & Waits**  
  - Default page-load timeout: 10 seconds  
  - Adjust in `MentiBotUi` where the `WebDriverWait` is instantiated:

    ```java
    new WebDriverWait(driver, Duration.ofSeconds(10))
    ```

- **CSS & Script Error Handling**  
  - CSS disabled, script errors suppressed by default.  
  - You can customize via `WebClient.getOptions()` calls in `MentiBotUi`.

## Usage

1. Launch the application.  
2. Enter the **slide-deck code** (the short alphanumeric ID you get from Mentimeter).  
3. Click **Fetch Questions**.  
4. Watch console logs for stage timings and possible warnings.  
5. When ready, the questions and choices appear in the text area.

## Logging & Diagnostics

Each stage prints a timestamped duration to the console:

```
Fetched participation key: alkfm9hkavgj (took 690 ms)
Initialized HtmlUnitDriver (took 687 ms)
Page loaded and JS data available (took 3748 ms)
Extracted JSON payload (took 120 ms)
Parsed questions & built output (took 35 ms)
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       ├── MentiBotApp.java         # Swing UI & orchestration
│   │       ├── ParticipationKeyFetcher.java
│   │       ├── MentimeterService.java  # headless browser + JSON extraction
│   │       └── model/
│   │           └── Question.java       # data model for question + choices
│   └── resources/
└── test/                              
```

## Contributing

1. Fork the repository.  
2. Create a feature branch: `git checkout -b feature/your-feature`.  
3. Commit your changes with clear messages.  
4. Push to your fork and open a Pull Request.  
