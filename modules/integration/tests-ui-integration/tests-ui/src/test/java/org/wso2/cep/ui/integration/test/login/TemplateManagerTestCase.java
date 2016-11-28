package org.wso2.cep.ui.integration.test.login;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
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
 * Created by sajith on 11/25/16.
 */
public class TemplateManagerTestCase extends CEPIntegrationUITest {
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();

    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Venerability in template manager")
    public void testXSSVenerability() throws Exception {
        boolean isVulnerable = false;
        // Login
        try {
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();


        String url = backendURL.substring(0, 22) + "/carbon/template-manager/domain_configurations_ajaxprocessor.jsp?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("ordinal", "1"));
        params.add(new BasicNameValuePair("domainName", "TemperatureAnalysis53151'+alert(1)//259"));
        url += URLEncodedUtils.format(params, "UTF-8");

        driver.get(url);

        Thread.sleep(1000 * 5);

        // Click the button
        driver.findElement(By.cssSelector("button[type=\"button\"]")).click();

        WebDriverWait webDriverWait = new WebDriverWait(driver, 5);
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("vulnerable")));
        } catch (UnhandledAlertException e){
            // If venerable the alert will pop
            isVulnerable = true;
        }
        catch (Exception e){

        }

        Assert.assertFalse(isVulnerable);
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
