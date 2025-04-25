package org.example.service.http;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class PageLoader {
    private final DriverFactory factory;

    public PageLoader(DriverFactory factory) {
        this.factory = factory;
    }

    public String loadRawData(String participationKey) {
        var driver = factory.createDriver();
        try {
            driver.get("https://www.menti.com/" + participationKey);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> Boolean.TRUE.equals(
                            ((JavascriptExecutor)d)
                                    .executeScript("return window.__next_f && window.__next_f.length>1;")));
            return (String)((JavascriptExecutor)driver).executeScript(
                    "return window.__next_f.filter(x=>x[0]===1).map(x=>x[1]).join('');");
        } finally {
            driver.quit();
        }
    }
}
