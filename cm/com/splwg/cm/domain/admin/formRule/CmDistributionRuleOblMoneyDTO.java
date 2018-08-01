package com.splwg.cm.domain.admin.formRule;

import java.util.Map;

import com.splwg.base.api.datatypes.Money;

public class CmDistributionRuleOblMoneyDTO {

	
	private Map<String, Money> mapOblMoney; //obligation,Money 

	public Map<String, Money> getMapOblMoney() {
		return mapOblMoney;
	}

	public void setMapOblMoney(Map<String, Money> mapOblMoney) {
		this.mapOblMoney = mapOblMoney;
	}
	
	
}
