package org.example.service.http;

import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;
import org.example.config.AppConfig;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class DriverFactory {
    public HtmlUnitDriver createDriver() {
        HtmlUnitDriver driver = new HtmlUnitDriver(AppConfig.isJavascriptEnabled());
        var wc = driver.getWebClient();

        wc.getOptions().setCssEnabled(AppConfig.isCssEnabled());
        wc.getOptions().setThrowExceptionOnScriptError(AppConfig.throwOnScriptError());
        wc.getOptions().setThrowExceptionOnFailingStatusCode(AppConfig.throwOnFailingStatusCode());
        wc.setCssErrorHandler(new DefaultCssErrorHandler());
        return driver;
    }
}
