package com.lambdatest.Tests;

import org.testng.annotations.Test;

import com.library.utils.testrail.TestRailHandler;

import org.testng.AssertJUnit;

import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class SingleTest {

	public static WebDriver driver;
	public static String status = "failed";

	@BeforeTest(alwaysRun=true)
	public void setUp() throws Exception {

		String browser = Configuration.readConfig("browser");
		String version = Configuration.readConfig("version");
		String os = Configuration.readConfig("os");
		String res = Configuration.readConfig("resolution");

		String username = System.getenv("LT_USERNAME") != null ? System.getenv("LT_USERNAME")
				: Configuration.readConfig("LambdaTest_UserName");
		String accesskey = System.getenv("LT_ACCESS_KEY") != null ? System.getenv("LT_ACCESS_KEY")
				: Configuration.readConfig("LambdaTest_AppKey");

		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(CapabilityType.BROWSER_NAME, browser);
		capability.setCapability(CapabilityType.VERSION, version);
		capability.setCapability(CapabilityType.PLATFORM, os);
		capability.setCapability("build", "TestNG Single Test");
		capability.setCapability("name", "TestNG Single");
		capability.setCapability("screen_resolution", res);
		capability.setCapability("network", true);
		capability.setCapability("video", true);
		capability.setCapability("console", true);
		capability.setCapability("visual", true);

		String gridURL = "http://" + username + ":" + accesskey + "@hub.lambdatest.com/wd/hub";
		try {
			driver = new RemoteWebDriver(new URL(gridURL), capability);
		} catch (Exception e) {
			System.out.println("driver error");
			System.out.println(e.getMessage());
		}
	}

	@Test
	public static void test() throws Exception {
		
		String testrailusername = System.getenv("TESTRAIL_USERNAME") != null ? System.getenv("TESTRAIL_USERNAME")
				: Configuration.readConfig("TESTRAIL_USERNAME");
		String testrailpassword = System.getenv("TESTRAIL_PASSWORD") != null ? System.getenv("TESTRAIL_PASSWORD")
				: Configuration.readConfig("TESTRAIL_PASSWORD");
		String testrailurl = System.getenv("TESTRAIL_URL") != null ? System.getenv("TESTRAIL_URL")
				: Configuration.readConfig("TESTRAIL_URL");
		
		TestRailHandler trh = new TestRailHandler(testrailusername,testrailpassword, testrailurl);
		try {

			// Launch the app
			driver.get("https://lambdatest.github.io/sample-todo-app/");

			// Click on First Item
			driver.findElement(By.name("li1")).click();

			// Click on Second Item
			driver.findElement(By.name("li2")).click();

			// Add new item is list
			driver.findElement(By.id("sampletodotext")).clear();
			driver.findElement(By.id("sampletodotext")).sendKeys("Yey, Let's add it to list");
			driver.findElement(By.id("addbutton")).click();

			// Verify Added item
			String item = driver.findElement(By.xpath("/html/body/div/div/div/ul/li[6]/span")).getText();
			AssertJUnit.assertTrue(item.contains("Yey, Let's add it to list"));
			status = "passed";
			System.out.println("here");
			((JavascriptExecutor) driver).executeScript("lambda-status=" + status + "");
			
			trh.updateResultToTestRail(1, "3351", "215");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			trh.updateResultToTestRail(1, "3351", "215");
		} catch (Error e) {
			System.out.println("Assert failed");
			trh.updateResultToTestRail(1, "3351", "215");
		}

	}

	@AfterTest
	public static void afterTest() {
		((JavascriptExecutor) driver).executeScript("lambda-status=" + status + "");
		driver.quit();
	}

}