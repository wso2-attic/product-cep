package org.wso2.cep.ui.integration.test.login;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationUITest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajith on 11/24/16.
 */
public class EventTracerTestCase extends CEPIntegrationUITest {
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();

    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Venerability in event tracer")
    public void testXSSVenerability() throws Exception {
        boolean isVulnerable = false;

        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();

        //Sending request to even-tracer admin service
        String url = backendURL.substring(0, 22) + "/carbon/event-tracer/index.jsp?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("ordinal", "1"));
        params.add(new BasicNameValuePair("op", "search"));
        params.add(new BasicNameValuePair("key", "<script>document.getElementById(\"dcontainer\").id=\"vaunerable\";</script>"));
        params.add(new BasicNameValuePair("ignoreCase", "false"));
        url += URLEncodedUtils.format(params, "UTF-8");

        driver.get(url);
        WebDriverWait webDriverWait = new WebDriverWait(driver, 5);

        try {
            webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("vaunerable")));
            // If not vulnerable, element with #vulnerable would not get injected
            // Therefore it'll throw an exception mentioning that.
            isVulnerable = true;
        } catch (Exception ignored) {
        }
        Assert.assertFalse(isVulnerable);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
