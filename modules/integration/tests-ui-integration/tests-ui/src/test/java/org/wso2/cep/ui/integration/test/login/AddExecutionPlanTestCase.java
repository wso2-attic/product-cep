/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cep.ui.integration.test.login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.cep.integration.common.utils.CEPIntegrationUITest;

public class AddExecutionPlanTestCase extends CEPIntegrationUITest{
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
    }

    @Test(groups = "wso2.cep", description = "verify adding an execution plan via management-console UI")
    public void testAddExecutionPlan() throws Exception {
        driver.get(getLoginURL());
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(cepServer.getContextTenant().getContextUser().getUserName());
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(cepServer.getContextTenant().getContextUser().getPassword());
        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.linkText("Execution Plans")).click();
        driver.findElement(By.linkText("Add Execution Plan")).click();
        driver.findElement(By.cssSelector("option[value=\"inStream:1.0.0\"]")).click();
        driver.findElement(By.id("importedStreamAs")).clear();
        driver.findElement(By.id("importedStreamAs")).sendKeys("inStream");
        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.xpath("//table[@id='eventProcessorAdd']/tbody/tr[2]/td/table/tbody/tr[4]/td/table/tbody/tr/td/div/div[6]/div/div/div/div/div[5]/div[11]/pre")).click();
        driver.findElement(By.id("exportedStreamValueOf")).clear();
        driver.findElement(By.id("exportedStreamValueOf")).sendKeys("outStream");
        new Select(driver.findElement(By.id("exportedStreamId"))).selectByVisibleText("outStream:1.0.0");
        driver.findElement(By.cssSelector("#exportedStreamId > option[value=\"outStream:1.0.0\"]")).click();
        driver.findElement(By.id("exportedStreamId")).click();
        new Select(driver.findElement(By.id("exportedStreamId"))).selectByVisibleText("outStream:1.0.0");
        driver.findElement(By.cssSelector("#exportedStreamId > option[value=\"outStream:1.0.0\"]")).click();
        driver.findElement(By.xpath("//input[@value='Export']")).click();
        driver.findElement(By.cssSelector("td.buttonRow > input[type=\"button\"]")).click();
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if(driver != null){
            driver.quit();
        }
    }
}
