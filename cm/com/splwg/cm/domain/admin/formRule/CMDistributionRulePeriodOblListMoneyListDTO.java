package com.splwg.cm.domain.admin.formRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.splwg.base.api.datatypes.Money;

public class CMDistributionRulePeriodOblListMoneyListDTO {

	  
	private Map<String, HashMap<List<String>, List<Money>>> periodOblMoney ; //period,list of obligation,list of money

	public Map<String, HashMap<List<String>, List<Money>>> getPeriodOblMoney() {
		return periodOblMoney;
	}

	public void setPeriodOblMoney(Map<String, HashMap<List<String>, List<Money>>> periodOblMoney) {
		this.periodOblMoney = periodOblMoney;
	} 
	
	
}
