package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;


public class Testing {
	private static String dellRepIreland =  "pedro_haeser_dell_rep@dell.com.dfs.dit";
	private static String dellRepFrance =  "pedro_haeser_dell_rep_france@dell.com.dfs.dit";
	private static String password = "d1n4m173,";
	private static String WEBDRIVERPATH = "C:\\workspaces\\IEDriverServer.exe";
	
	private WebDriver driver;
	private String baseUrl;
	
	private ArrayList<Integer> terms = new ArrayList<Integer>();
	private ArrayList<Integer> frequencies = new ArrayList<Integer>();
	private Map<Integer,String> frequencies2String = new HashMap<Integer,String>();
	
	public Testing(){
		populateTerms();
		populateFrequencies();
		populateFrequencies2String();
		System.setProperty("webdriver.ie.driver", WEBDRIVERPATH);
	}
	
	@Before
	public void setUp() throws Exception {
		driver = new InternetExplorerDriver();
		baseUrl = "https://dit-dfs.cs30.force.com";
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
	}
		
	@Test
	public void leaseCalculatorIreland() throws Exception{
		openExternalPortal(Testing.dellRepIreland,Testing.password);
		String leaseType = "Residual Based Lease";
		Double equipmentCost = 500.0;
		Double commission = 0.00;
		Integer numOfAssets = 1;
		Double categoryAmount = 500.0;
		String assetCategory = "Dell Client Equipment";

		for(Integer t:terms)
			for(Integer f:frequencies)
				tryLeaseCalculator(leaseType, equipmentCost, commission, t, f, assetCategory, numOfAssets, categoryAmount);
	}

	@Test
	public void leaseCalculatorIrelandFinanceLeaseResidual() throws Exception{
		openExternalPortal(Testing.dellRepIreland,Testing.password);
		String leaseType = "Finance Lease Residual";
		Double equipmentCost = 500.0;
		Double commission = 0.00;
		Integer numOfAssets = 1;
		Double categoryAmount = 500.0;
		String assetCategory = "Dell Client Equipment";

		for(Integer t:terms)
			for(Integer f:frequencies)
				tryLeaseCalculator(leaseType, equipmentCost, commission, t, f, assetCategory, numOfAssets, categoryAmount);
	}

	@Test
	public void leaseCalculatorFrance() throws Exception{
		openExternalPortal(Testing.dellRepFrance,Testing.password);
		String leaseType = "Crédit-Bail";
		Double equipmentCost = 500.0;
		Double commission = 0.01;
		Integer numOfAssets = 1;
		Double categoryAmount = 500.0;
		String assetCategory = "IT Related Hardware";

		for(Integer t:terms)
			for(Integer f:frequencies)
				tryLeaseCalculator(leaseType, equipmentCost, commission, t, f, assetCategory, numOfAssets, categoryAmount);
	}
	
	private void openExternalPortal(String username, String password) throws Exception {
		driver.get(baseUrl + "/fcportal/login");
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("Login")).click();
	}

    private void logoutExternalPortal() throws Exception {
        driver.findElement(By.className("block_username")).findElement(By.tagName("a")).click();
    }

	public void tryLeaseCalculator(String leaseType, Double equipmentCost, Double commission, Integer term, Integer frequency, String assetCategory, Integer numOfAssets, Double categoryAmount) throws Exception {

		LeaseCalculator lc = new LeaseCalculator();
		tryLeaseCalculator(leaseType, String.valueOf(equipmentCost), String.valueOf(term), this.frequencies2String.get(frequency), assetCategory, String.valueOf(numOfAssets), String.valueOf(categoryAmount),
				lc.leaseCalculate(leaseType, equipmentCost, commission, term, frequency, assetCategory, numOfAssets, categoryAmount));
	}

	public void tryLeaseCalculator(String leaseType, String equipmentCost, String term, String frequency, String assetCategory, String numOfAssets, String categoryAmount, Double expectedRentalValue) throws Exception {
		//Lease Calculator
		driver.findElement(By.className("icon_lease")).click();
		Thread.sleep(1000);
		// Equipment Cost
		driver.findElement(By.name("calculation-input-value")).sendKeys(equipmentCost);
		// Lease Type
		driver.findElement(By.name("Deal_Type__c")).click();
		WebElement leaseTypes = driver.findElement(By.name("Deal_Type__c"));
		leaseTypes.sendKeys(leaseType);
		leaseTypes.click();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// Term
		String termScript = "document.getElementsByName('Lease_Term__c').item(0).setAttribute('value', '" + term + "')";
		js.executeScript(termScript);
		// Frequency
		String frequencyScript = "document.getElementsByName('Payment_Frequency__c').item(0).setAttribute('value', '" + frequency+ "')";
		js.executeScript(frequencyScript);
		// Number of Assets
		driver.findElements(By.name("Quantity")).get(2).sendKeys(numOfAssets);
		// Category Amount
		driver.findElements(By.name("Total_Price__c")).get(2).sendKeys(categoryAmount);
		//Wait until it calculates
		WebDriverWait wait = new WebDriverWait(driver,10);
		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
			    WebElement elementWhole = driver.findElements(By.className("currency-whole")).get(2);
			    WebElement elementFraction = driver.findElements(By.className("currency-fraction")).get(2);
			    String whole = elementWhole.getText();
			    String fraction = elementFraction.getText();
			    if(whole.equals("0") && fraction.equals("00"))
			    	return false;
			    else
			    	return true;
		    }
			});
		
//		String symbol = driver.findElements(By.className("currency-symbol-left")).get(2).getText();
		String whole = driver.findElements(By.className("currency-whole")).get(2).getText();
		String fraction = driver.findElements(By.className("currency-fraction")).get(2).getText();
		Double rentalValue = Double.parseDouble(whole+"."+fraction);
		
		System.out.println("Testing: " + "Term = " + term + " Frequency = " + frequency);
		
		Assert.assertEquals(expectedRentalValue, rentalValue, 0.01);
	}
	
	
		
	private void populateTerms(){
		this.terms.add(12);
		this.terms.add(24);
		this.terms.add(36);
		this.terms.add(48);
	}

	private void populateFrequencies(){
		this.frequencies.add(1);
		this.frequencies.add(2);
		this.frequencies.add(4);
		this.frequencies.add(12);
	}

	private void populateFrequencies2String(){
		this.frequencies2String.put(new Integer(1),"Annually");
		this.frequencies2String.put(new Integer(2),"Semi Annually");
		this.frequencies2String.put(new Integer(4),"Quarterly");
		this.frequencies2String.put(new Integer(12),"Monthly");
	}
		
	@After
	public void tearDown() throws Exception {
		logoutExternalPortal();
		Thread.sleep(2000);
		driver.quit();
	}

}
