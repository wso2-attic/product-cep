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

package org.wso2.cep.ui.integration.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationUITest;

import java.net.MalformedURLException;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertEquals;
import static org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants.DASHBOARD_REGISTRY_BASE_PATH;
import static org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants.DS_SUFFIX;


public class DashboardTestCase extends CEPIntegrationUITest {
    private WebDriver driver;
    private final String DASHBOARD_TITLE = "sample_dashboard";
    private WebDriverWait wait = null;
    private WebElement webElement = null;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        resourcePath = DASHBOARD_REGISTRY_BASE_PATH + DASHBOARD_TITLE;
        login(getCurrentUsername(), getCurrentPassword());
    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Venerability in portal dashboard settings and create pages")
    public void testXSSVenerability() throws Exception {

        // testing for XSS Venerability in portal dashboard create page
        String DASHBOARD_DESCRIPTION = "></script><script>alert('hi')</script>";
        String afterDashboardDes = "scriptscriptalerthiscript";
        String afterDashboardTitle = "sampledashboard";
        redirectToLocation("portal", "dashboards");
        driver.findElement(By.cssSelector("[href='create-dashboard']")).click();
        driver.findElement(By.id("ues-dashboard-title")).clear();
        driver.findElement(By.id("ues-dashboard-title")).sendKeys(DASHBOARD_TITLE);
        driver.findElement(By.id("ues-dashboard-description")).clear();
        driver.findElement(By.id("ues-dashboard-description")).sendKeys(DASHBOARD_DESCRIPTION);
        driver.findElement(By.id("ues-dashboard-create")).click();
        driver.findElement(By.cssSelector("div[data-id='single-column']")).click();
        redirectToLocation("portal", "dashboards");
        getWebDriverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("ues-dashboard-title")));
        webElement = driver.findElement(By.id("ues-dashboard-title"));
        assertEquals(afterDashboardTitle, webElement.findElement(By.id("ues-dashboard-title")).getText());
        webElement = driver.findElement(By.id("ues-dashboard-description"));
        assertEquals(afterDashboardDes, webElement.findElement(By.id("ues-dashboard-description")).getText());

        // testing for XSS Venerability in portal dashboard settings page
        driver.findElement(By.id("ues-settings")).click();
        driver.findElement(By.id("ues-dashboard-description")).clear();
        driver.findElement(By.id("ues-dashboard-description")).sendKeys(DASHBOARD_DESCRIPTION);
        driver.findElement(By.id("ues-dashboard-saveBtn")).click();
        redirectToLocation("portal", "dashboards");
        getWebDriverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("ues-dashboard-title")));
        webElement = driver.findElement(By.id("ues-dashboard-title"));
        assertEquals(afterDashboardTitle, webElement.findElement(By.id("ues-dashboard-title")).getText());
        webElement = driver.findElement(By.id("ues-dashboard-description"));
        assertEquals(afterDashboardDes, webElement.findElement(By.id("ues-dashboard-description")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     * Redirect user to given location
     *
     * @param domain   name of the domain where user wants to direct in to
     * @param location name of the location to be directed to
     */
    public void redirectToLocation(String domain, String location) throws Exception {
        String url = getBaseUrl() + "/" + domain;
        if (location != null && !location.isEmpty()) {
            url += "/" + location;
        }
        driver.get(url);
    }

    /**
     * To login to Dashboard server
     *
     * @param userName user name
     * @param pwd      password
     * @throws XPathExpressionException,InterruptedException
     */
    public void login(String userName, String pwd) throws Exception {
        String fullUrl = "", currentUrl = "";
        fullUrl = getBaseUrl() + DS_SUFFIX;
        driver.get(fullUrl);
        currentUrl = driver.getCurrentUrl();
        driver.findElement(By.name("username")).clear();
        driver.findElement(By.name("username")).sendKeys(userName);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(pwd);
        if (currentUrl.contains("authenticationendpoint/login.do")) { // sso login enabled
            driver.findElement(By.tagName("button")).click();
        } else { // basic login enabled
            driver.findElement(By.cssSelector(".ues-signin")).click();
        }
    }

    /**
     * This method returns the we driver wait instance
     *
     * @return DSWEbDriverWait - the webDriverWait instance of DSWebDriverWait
     */
    public WebDriverWait getWebDriverWait() throws MalformedURLException, XPathExpressionException {
        if (wait == null) {
            wait = new WebDriverWait(driver, getMaxWaitTime());
        }
        return wait;
    }

    /**
     * This method will return maximum waiting time for web driver in automation.xml
     *
     * @return waitingTime in seconds configured in automation.xml
     * @throws XPathExpressionException
     */
    public int getMaxWaitTime() throws XPathExpressionException {
        return Integer.parseInt(getDsContext().getConfigurationValue("//maximumWaitingTime"));
    }
}
