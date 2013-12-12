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

public class Testing {
	private static String dellRep =  "pedro_haeser_dell_rep@dell.com.dfs.dit";
	private static String pwd = "d1n4m173,";
	private static Double TYPE = 1.0;
	
	private WebDriver driver;
	private String baseUrl;
	private String username;
	private String password;
	
	private ArrayList<Integer> terms = new ArrayList<Integer>();
	private ArrayList<Integer> frequencies = new ArrayList<Integer>();
	private Map<Integer,String> frequencies2String = new HashMap<Integer,String>();
	private Map<Integer,Double> costOfFunds = new HashMap<Integer,Double>();
	private Map<Integer,Double> term2BlendedRV = new HashMap<Integer,Double>();
	private Map<Integer,Double> term2LeaseRV = new HashMap<Integer,Double>();
	private Map<String,Map<Integer,Double>> leaseType2TermRV = new HashMap<String,Map<Integer,Double>>();
	
	@Before
	public void setUp() throws Exception {
		System.setProperty("webdriver.ie.driver", "C:\\Users\\Pedro_Haeser\\Desktop\\LeaseCalculatorSelenium\\IEDriverServer.exe");
		driver = new InternetExplorerDriver();
		baseUrl = "https://dit-dfs.cs30.force.com";
		username = dellRep;
		password = pwd;
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		
		populateTerms();
		populateFrequencies();
		populateFrequencies2String();
		populateLeaseType2TermRV();
		populateTerm2BlendedRV();
		populateTerm2LeaseRV();
		populateCostOfFunds();
		
	}
	
	private void openExternalPortal() throws Exception {
		driver.get(baseUrl + "/fcportal/login");
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("Login")).click();
	}
	
	@Test
	public void leaseCalculatorSuite() throws Exception{
		openExternalPortal();
		String leaseType = "Residual Based Lease";
		Double equipmentCost = 500.0;
		Integer numOfAssets = 1;
		Double categoryAmount = 500.0;
		String assetCategory = "Dell Client Equipment";

		for(Integer t:terms)
			for(Integer f:frequencies)
				tryLeaseCalculator(leaseType, equipmentCost, t, f, assetCategory, numOfAssets, categoryAmount);
	}
	
	
	public void tryLeaseCalculator(String leaseType, Double equipmentCost, Integer term, Integer frequency, String assetCategory, Integer numOfAssets, Double categoryAmount) throws Exception {

		tryLeaseCalculator(leaseType, String.valueOf(equipmentCost), String.valueOf(term), this.frequencies2String.get(frequency), assetCategory, String.valueOf(numOfAssets), String.valueOf(categoryAmount),
				leaseCalculate(leaseType, equipmentCost, term, frequency, assetCategory, numOfAssets, categoryAmount));
	}

	public void tryLeaseCalculator(String leaseType, String equipmentCost, String term, String frequency, String assetCategory, String numOfAssets, String categoryAmount, String expectedRentalValue) throws Exception {
		//Lease Calculator
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
		// Number of Assets
		driver.findElements(By.name("Quantity")).get(2).sendKeys(numOfAssets);
		// Category Amount
		driver.findElements(By.name("Total_Price__c")).get(2).sendKeys(categoryAmount);
		//Wait until it calculates
		Thread.sleep(3500);
		
		String whole = driver.findElements(By.className("currency-whole")).get(2).getText();
		String fraction = driver.findElements(By.className("currency-fraction")).get(2).getText();
		String rentalValue = whole+"."+fraction;
		
		double rentalValueD = Double.parseDouble(rentalValue);
		double expectedRentalValueD = Double.parseDouble(expectedRentalValue);
		
		System.out.println("Testing: " + "Term = " + term + " Frequency = " + frequency);
		
		Assert.assertEquals(expectedRentalValueD, rentalValueD, 0.01);		
	}
	
	public String leaseCalculate(String leaseType, Double equipmentCost, Integer term, Integer frequency, String assetCategory, Integer numOfAssets, Double categoryAmount){
		
		double rental = 0.0;
		double presentValue = categoryAmount;
		double nper =  term/(12/frequency);
		double marginRate = getMarginRate(leaseType, term);
//		System.out.println("Margin Rate: "+marginRate);
		double costOfFunds = getCostOfFunds(term);
//		System.out.println("Cost of Funds: "+costOfFunds);
		double rate = (marginRate/frequency) + costOfFunds;
		double residualValue = getResidualValue(leaseType, assetCategory, term);
//		System.out.println("RV: "+residualValue);
		double futureValue = presentValue*residualValue;
		double type = TYPE;

		rental = ((presentValue*(Math.pow((1.0+rate),nper)))-futureValue) / ((1.0+(type*rate))*((Math.pow((1.0+rate),nper))-1.0)/rate);
//		System.out.println("Rental: "+rental);
		return String.valueOf(rental);
	}
	
	private Double getResidualValue(String leaseType, String assetCategory, Integer term) {
		// TODO Add parameters business segments, eboss branch, currency ...
		return this.leaseType2TermRV.get(leaseType).get(term);
	}

	
	private Double getCostOfFunds(Integer term) {
		// TODO Add parameter currency
		return this.costOfFunds.get(term);
	}
	
	private Double getMarginRate(String leaseType, int term) {
		// TODO Add parameters business segments, eboss branch, currency ...
		Double margin = 0.0;
		switch(term){
			case 48 : margin = 0.050; break;
			case 36 : margin = 0.050; break;
			case 24 : margin = 0.070; break;
			case 12 : margin = 0.070; break;
			default : margin = null; 
		}
		return margin;
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
	
	private void populateLeaseType2TermRV() {
		this.leaseType2TermRV.put("Residual Based Value", term2BlendedRV);
		this.leaseType2TermRV.put("Finance Lease Residual", term2LeaseRV);
	}

	private void populateTerm2BlendedRV() {
		this.term2BlendedRV.put(new Integer(48), new Double(0.0105));
		this.term2BlendedRV.put(new Integer(36), new Double(0.0105));
		this.term2BlendedRV.put(new Integer(24), new Double(0.0125));
		this.term2BlendedRV.put(new Integer(12), new Double(0.0125));
	}

	private void populateTerm2LeaseRV() {
		this.term2LeaseRV.put(new Integer(48), new Double(0.0160));
		this.term2LeaseRV.put(new Integer(36), new Double(0.0160));
		this.term2LeaseRV.put(new Integer(24), new Double(0.0120));
		this.term2LeaseRV.put(new Integer(12), new Double(0.0120));
	}
	
	private void populateCostOfFunds() {
		this.costOfFunds.put(new Integer(48), new Double(0.0100));
		this.costOfFunds.put(new Integer(36), new Double(0.0309));
		this.costOfFunds.put(new Integer(24), new Double(0.0100));
		this.costOfFunds.put(new Integer(12), new Double(0.0100));
	}
	
	@After
	public void tearDown() throws Exception {
		Thread.sleep(2000);
		driver.quit();
	}

}
