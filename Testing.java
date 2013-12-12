package test;

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

public class Testing {
	private static String dellRep =  "lucas_rigo_dell_rep@dell.com.dfs.dit";
	private static String pwd = "SFDCdit123";
	private static Double TYPE = 1.0;
	
	private WebDriver driver;
	private String baseUrl;
	private String username;
	private String password;
	
	@Before
	public void setUp() throws Exception {
		System.setProperty("webdriver.ie.driver", "C:\\workspaces\\IEDriverServer.exe");
		driver = new InternetExplorerDriver();
		baseUrl = "https://dit-dfs.cs30.force.com";
		username = dellRep;
		password = pwd;
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
	}
	
	private void openExternalPortal() throws Exception {
		driver.get(baseUrl + "/fcportal/login");
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("loginwidget")).click();
	}
	
	@Test
	public void leaseCalculatorSuite() throws Exception{
		openExternalPortal();
		Double equipmentCost = 500.0;
		String leaseType = "Residual Based Lease";
		Double term = 48.0;
		Double frequency = 12.0;
		String assetCategory = "Dell Client Equipment";
		Integer numOfAssets = 1;
		Double categoryAmount = 500.0;
				
		tryLeaseCalculator(String.valueOf(equipmentCost),leaseType,String.valueOf(term),frequencyToString(frequency),String.valueOf(numOfAssets),String.valueOf(categoryAmount), 
				leaseCalculate(equipmentCost,leaseType,term,frequency,assetCategory,numOfAssets,categoryAmount));
	}


	public void tryLeaseCalculator(String equipmentCost, String leaseType, String term, String frequency, String numOfAssets, String categoryAmount, Double expectedRentalValue) throws Exception {
		driver.findElement(By.className("icon_lease")).click();
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
		// Add Asset
		driver.findElement(By.name("add-button")).click();
		// Number of Assets
		driver.findElements(By.name("Quantity")).get(1).sendKeys(numOfAssets);
		// Category Amount
		driver.findElements(By.name("Total_Price__c")).get(1).sendKeys(categoryAmount);
		// Use for Quick Quote
		driver.findElement(By.id("quickquote")).click();
		//Wait until it calculates
		Thread.sleep(2000);
		String currency = driver.findElements(By.className("currency-symbol-left")).get(2).getText();
		String whole = driver.findElements(By.className("currency-whole")).get(2).getText();
		String fraction = driver.findElements(By.className("currency-fraction")).get(2).getText();
		String rentalValue = currency+whole+"."+fraction;
		Assert.assertEquals(expectedRentalValue, Double.valueOf(rentalValue), 0.01);
	}
	
	public Double leaseCalculate(Double equipmentCost, String leaseType, Double term, Double frequency, String assetCategory, Integer numOfAssets, Double categoryAmount){
		Double rental = 0.0;
		Double presentValue = categoryAmount;
		Double nper =  term/(12/frequency);
		Double marginRate = getMarginRate(leaseType, term);
		Double costOfFunds = getCostOfFunds(term);
		Double rate = (marginRate/frequency) + costOfFunds;
		Double residualValue = getResidualValue(assetCategory,term);
		Double futureValue = presentValue*residualValue;
		Double type = TYPE;
		
		Double rentalcima = ((presentValue*(Math.pow((1.0+rate),nper)))-futureValue);
		Double rentalbaixo = ((1.0+(type*rate))*((Math.pow((1.0+rate),nper))-1.0)/rate);
		rental =  rentalcima / rentalbaixo ;
		System.out.println("rental = "+rental+"\nrentalcima = "+rentalcima+"\nrentalbaixo = "+rentalbaixo);
		return rental;
	}
	
	private Double getResidualValue(String assetCategory, Double term) {
		// TODO Add parameters business segments, eboss branch, currency ...
		Double rv = 0.0;
		switch(term.intValue()){
			case 48 : rv = 1.05; break;
			case 36 : rv = 1.05; break;
			case 24 : rv = 1.25; break;
			case 12 : rv = 1.25; break;
			default : rv = null; 
		}
		return rv;
	}

	private Double getCostOfFunds(Double term) {
		// TODO Add parameter currency
		Double cof = 0.0;
		switch(term.intValue()){
			case 48 : cof = 1.0; break;
			case 36 : cof = 3.09; break;
			case 24 : cof = 1.0; break;
			case 12 : cof = 1.0; break;
			default : cof = null; 
		}
		return cof;
	}

	private Double getMarginRate(String leaseType, Double term) {
		// TODO Add parameters business segments, eboss branch, currency ...
		Double margin = 0.0;
		switch(term.intValue()){
			case 48 : margin = 5.0; break;
			case 36 : margin = 5.0; break;
			case 24 : margin = 7.0; break;
			case 12 : margin = 7.0; break;
			default : margin = null; 
		}
		return margin;
	}

	private String frequencyToString(Double frequency) {
		String freq = "";
		switch(frequency.intValue()){
			case 12 : freq = "Monthly"; break;
			case 4 : freq = "Quarterly"; break;
			case 2 : freq = "Semi Annually"; break;
			case 1 : freq = "Annually"; break;
			default : freq = null; 
		}
		return freq;
	}
	
	@After
	public void tearDown() throws Exception {
		Thread.sleep(2000);
		driver.quit();
	}

}
