/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cep.ui.integration.test.login;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationUITest;

import java.util.ArrayList;
import java.util.List;

public class EventSimulatorTestCase extends CEPIntegrationUITest {
    private WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        String loggedInSessionCookie = getSessionCookie();

        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);

        // Deploying the stream definition required to run the simulator
        String streamDefinitionAsString = getJSONArtifactConfiguration("eventsimulatorFiles",
                "TempStream_1.0.0.json");

        eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), 1);
    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Venerability in event simulator")
    public void testXSSVenerability() throws Exception {
        boolean isVulnerable = false;

        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.id("menu-panel-button4")).click();

        // Goto Event Simulator page
        String pageUrl = backendURL.substring(0, 22) + "/carbon/eventsimulator/index.jsp?";
        List<NameValuePair> pageParams = new ArrayList<>();
        pageParams.add(new BasicNameValuePair("region", "5"));
        pageParams.add(new BasicNameValuePair("item", "event_simulator_menu"));
        pageUrl += URLEncodedUtils.format(pageParams, "UTF-8");

        driver.get(pageUrl);
        WebDriverWait webDriverWait = new WebDriverWait(driver, 5);

        // Fill the event field values and click send
        Select dropdown = new Select(driver.findElement(By.id("EventStreamID")));
        dropdown.selectByVisibleText("TempStream:1.0.0");
        driver.findElement(By.id("0")).clear();
        driver.findElement(By.id("0")).sendKeys("1");
        driver.findElement(By.id("1")).clear();
        driver.findElement(By.id("1")).sendKeys("<script>document.getElementById(\\\"dcontainer\\\").id=\\\"vaunerable\\\";</script>");
        driver.findElement(By.id("2")).clear();
        driver.findElement(By.id("2")).sendKeys("3");
        driver.findElement(By.xpath("(//input[@value='Send'])[1]")).click();


        try {
            Thread.sleep(1000 * 5);
            webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("vaunerable")));
            // If not vulnerable, element with #vulnerable would not get injected
            // Therefore it'll throw an exception mentioning that.
            isVulnerable = true;
        } catch (Exception ignored) {
        }
        Assert.assertFalse(isVulnerable);
        driver.close();

    }

    @Test(groups = "wso2.cep", description = "Test CSRF issue in event simulator")
    public void testCSRF() throws Exception {
        boolean testPassed = false;
        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.id("menu-panel-button4")).click();

        // Goto Event Simulator page
        String pageUrl = backendURL.substring(0, 22) + "/carbon/eventsimulator/index.jsp?";
        List<NameValuePair> pageParams = new ArrayList<>();
        pageParams.add(new BasicNameValuePair("region", "5"));
        pageParams.add(new BasicNameValuePair("item", "event_simulator_menu"));
        pageUrl += URLEncodedUtils.format(pageParams, "UTF-8");

        driver.get(pageUrl);

        // Fill the event field values and click send
        Select dropdown = new Select(driver.findElement(By.id("EventStreamID")));
        dropdown.selectByVisibleText("TempStream:1.0.0");
        driver.findElement(By.id("0")).clear();
        driver.findElement(By.id("0")).sendKeys("11");
        driver.findElement(By.id("1")).clear();
        driver.findElement(By.id("1")).sendKeys("22");
        driver.findElement(By.id("2")).clear();
        driver.findElement(By.id("2")).sendKeys("33");
        driver.findElement(By.xpath("(//input[@value='Send'])[1]")).click();

        if("Events is successfully sent".equals(
                driver.findElement(By.id("messagebox-info")).findElement(By.tagName("p")).getText())) {
            testPassed = true;
        }

        Assert.assertTrue(testPassed);
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
