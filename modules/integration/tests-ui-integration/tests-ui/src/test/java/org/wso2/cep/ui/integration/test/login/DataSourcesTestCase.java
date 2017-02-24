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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationUITest;

public class DataSourcesTestCase extends CEPIntegrationUITest {
    private WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Vulnerability in event data sources - description field")
    public void testXSSVenerabilityDescriptionField() throws Exception {
        boolean isVulnerable = false;

        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();

        // Sending request to even-tracer admin service
        String url = backendURL.substring(0, 22) + "/carbon/ndatasource/newdatasource.jsp?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("description", "RiskScoringDB\"><script>alert(1)</script><example attr=\""));
        params.add(new BasicNameValuePair("edit", "true"));
        url += URLEncodedUtils.format(params, "UTF-8");

        driver.get(url);
        try {
            // Alert appears if vulnerable to XSS attack.
            Alert alert = driver.switchTo().alert();
            alert.accept();
            isVulnerable = true;
        } catch (NoAlertPresentException e) {
            // XSS vulnerability is not there
        }
        Assert.assertFalse(isVulnerable);
        driver.close();
    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Vulnerability in event data sources - driver field")
    public void testXSSVenerabilityDriverField() throws Exception {
        boolean isVulnerable = false;

        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();

        // Sending request to even-tracer admin service
        String url = backendURL.substring(0, 22) + "/carbon/ndatasource/validateconnection-ajaxprocessor.jsp?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("dsName", "John"));
        params.add(new BasicNameValuePair("driver", "<script>alert(1)</script>"));
        params.add(new BasicNameValuePair("url", "http://abc.com"));
        params.add(new BasicNameValuePair("username", "John"));
        params.add(new BasicNameValuePair("dsType", "RDBMS"));
        params.add(new BasicNameValuePair("dsProviderType", "default"));
        url += URLEncodedUtils.format(params, "UTF-8");

        driver.get(url);
        try {
            // Alert appears if vulnerable to XSS attack.
            Alert alert = driver.switchTo().alert();
            alert.accept();
            isVulnerable = true;
        } catch (NoAlertPresentException e) {
            // XSS vulnerability is not there
        }
        Assert.assertFalse(isVulnerable);
        driver.close();
    }

    @Test(groups = "wso2.cep", description = "Verifying XSS Vulnerability in event data sources - data source name field")
    public void testXSSVenerabilityNameField() throws Exception {
        boolean isVulnerable = false;

        // Login
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();

        // Sending request to even-tracer admin service
        String url = backendURL.substring(0, 22) + "/carbon/ndatasource/newdatasource.jsp?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("dsName", "RiskScoringDB\"><script>alert(1)</script><example attr=\""));
        params.add(new BasicNameValuePair("edit", "true"));
        url += URLEncodedUtils.format(params, "UTF-8");

        driver.get(url);
        try {
            // Alert appears if vulnerable to XSS attack.
            Alert alert = driver.switchTo().alert();
            alert.accept();
            isVulnerable = true;
        } catch (NoAlertPresentException e) {
            // XSS vulnerability is not there
        }
        Assert.assertFalse(isVulnerable);
        driver.close();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
