package com.splwg.cm.domain.common.entities;

import com.ibm.icu.math.BigDecimal;

public class InformationCalcPoint {

	private BigDecimal salaireRef;
	private BigDecimal tauxContractuel;
	private BigDecimal plafondSalAnnuel;
	
	public InformationCalcPoint() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InformationCalcPoint(BigDecimal salaireRef, BigDecimal tauxContractuel, BigDecimal plafondSalAnnuel) {
		super();
		this.salaireRef = salaireRef;
		this.tauxContractuel = tauxContractuel;
		this.plafondSalAnnuel = plafondSalAnnuel;
	}

	public BigDecimal getSalaireRef() {
		return salaireRef;
	}

	public void setSalaireRef(BigDecimal salaireRef) {
		this.salaireRef = salaireRef;
	}

	public BigDecimal getTauxContractuel() {
		return tauxContractuel;
	}

	public void setTauxContractuel(BigDecimal tauxContractuel) {
		this.tauxContractuel = tauxContractuel;
	}

	public BigDecimal getPlafondSalAnnuel() {
		return plafondSalAnnuel;
	}

	public void setPlafondSalAnnuel(BigDecimal plafondSalAnnuel) {
		this.plafondSalAnnuel = plafondSalAnnuel;
	}
	
	
	
}
