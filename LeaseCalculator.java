package test;

import java.util.HashMap;
import java.util.Map;

/*
Rules for selection:

Margin Grid (Dealer tier, Business Segment, Country, Term)
- If there’s more than one record: (Finance Type)

Residual Value (Lease Type)
- If Finance Lease Residual: Lease Type RV Grid (Business Segment, eBoss Branch, Local Lease Name(?), Term)
- If Purchase Option: Purchase Option Grid (eBoss Branch, Term)
- Else: Blended RV Grid (Asset Category, Business Segment, eBoss Branch, Term)

Cost of Funds (Currency, Term)

 */
public class LeaseCalculator {

	private static Double TYPE = 1.0;

	private Map<Integer,Double> costOfFunds = new HashMap<Integer,Double>();
	private Map<String,Map<Integer,Double>> leaseType2TermRV = new HashMap<String,Map<Integer,Double>>();
	private Map<Integer,Double> term2BlendedRV = new HashMap<Integer,Double>();
	private Map<Integer,Double> term2LeaseRV = new HashMap<Integer,Double>();
	private Map<Integer,Double> term2PurchaseRV = new HashMap<Integer,Double>();
	private Map<String,Map<Integer,Double>> leaseType2TermMR = new HashMap<String,Map<Integer,Double>>();
    private Map<Integer,Double> term2BlendedMR = new HashMap<Integer,Double>();
    private Map<Integer,Double> term2LeaseMR = new HashMap<Integer,Double>();    
    private Map<Integer,Double> term2PurchaseMR = new HashMap<Integer,Double>();

	
	
	public LeaseCalculator(){
		populateResidualValues();
		populateMarginRates();
		populateCostOfFunds();
	}

	public Double leaseCalculate(String leaseType, Double equipmentCost, Double commission, Integer term, Integer frequency, String assetCategory, Integer numOfAssets, Double categoryAmount){
		Double rental = new Double(0.0);
		Double presentValue = categoryAmount * (1.0+commission);
		Double nper =  new Double(term/(12/frequency));
		Double marginRate = getMarginRate(leaseType, term);
		Double costOfFunds = getCostOfFunds(term);
		Double rate = (marginRate/frequency) + costOfFunds;
		Double residualValue = getResidualValue(leaseType, assetCategory, term);
		Double futureValue = categoryAmount*residualValue;
		Double type = TYPE;

		rental = ((presentValue*(Math.pow((1.0+rate),nper)))-futureValue) / ((1.0+(type*rate))*((Math.pow((1.0+rate),nper))-1.0)/rate);
		return rental;
	}


	private Double getResidualValue(String leaseType, String assetCategory, Integer term) {
		// TODO Add parameters business segments, eboss branch, currency ...
		return this.leaseType2TermRV.get(leaseType).get(term);
	}
	
	private Double getCostOfFunds(Integer term) {
		// TODO Add parameter currency
		return this.costOfFunds.get(term);
	}
	
	private Double getMarginRate(String leaseType, Integer term) {
		return this.leaseType2TermMR.get(leaseType).get(term);
	}

	private void populateMarginRates() {
		populateLeaseType2TermMR();
	}

	private void populateResidualValues() {
		populateLeaseType2TermRV();
	}
	
	private void populateLeaseType2TermRV() {
		populateTerm2BlendedRV();
		populateTerm2LeaseRV();
		populateTerm2PurchaseRV();
		this.leaseType2TermRV.put("Residual Based Lease", term2BlendedRV);
		this.leaseType2TermRV.put("Finance Lease Residual", term2LeaseRV);
        this.leaseType2TermRV.put("Crédit-Bail", term2PurchaseRV);
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
	
    private void populateTerm2PurchaseRV() {
        this.term2PurchaseRV.put(new Integer(48), new Double(0.1700));
        this.term2PurchaseRV.put(new Integer(36), new Double(0.1700));
        this.term2PurchaseRV.put(new Integer(24), new Double(0.1500));
        this.term2PurchaseRV.put(new Integer(12), new Double(0.1500));
    }

    private void populateLeaseType2TermMR() {
        populateTerm2BlendedMR();
        populateTerm2PurchaseMR();
        populateTerm2LeaseMR();
        this.leaseType2TermMR.put("Residual Based Lease", term2BlendedMR);
        this.leaseType2TermMR.put("Finance Lease Residual", term2LeaseMR);
        this.leaseType2TermMR.put("Crédit-Bail", term2PurchaseMR);
    }
    
    private void populateTerm2PurchaseMR() {
        this.term2PurchaseMR.put(new Integer(48), new Double(0.0400));
        this.term2PurchaseMR.put(new Integer(36), new Double(0.0300));
        this.term2PurchaseMR.put(new Integer(24), new Double(0.0200));
        this.term2PurchaseMR.put(new Integer(12), new Double(0.0100));
  }     
  
    private void populateTerm2LeaseMR() {
        this.term2LeaseMR.put(new Integer(12), new Double (0.0700));
        this.term2LeaseMR.put(new Integer(24), new Double (0.0800));
        this.term2LeaseMR.put(new Integer(36), new Double (0.0900));
        this.term2LeaseMR.put(new Integer(48), new Double (0.1000));
  }

    private void populateTerm2BlendedMR() {
        this.term2BlendedMR.put(new Integer(12), new Double (0.0700));
        this.term2BlendedMR.put(new Integer(24), new Double (0.0700));
        this.term2BlendedMR.put(new Integer(36), new Double (0.0500));
        this.term2BlendedMR.put(new Integer(48), new Double (0.0500));
  }
      
	private void populateCostOfFunds() {
		this.costOfFunds.put(new Integer(48), new Double(0.0100));
		this.costOfFunds.put(new Integer(36), new Double(0.0309));
		this.costOfFunds.put(new Integer(24), new Double(0.0100));
		this.costOfFunds.put(new Integer(12), new Double(0.0100));
	}

}
