package com.splwg.cm.domain.common.entities;

import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Id;

public class CmPeriodicBillEntity implements Id {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2551293891146472409L;
	private String taxBillId;
	private String processFlowId;
	private String taxRoleId;
	private Date billStartDate;
	private Date billEndDate;

	public String getTaxBillId() {
		return taxBillId;
	}

	public void setTaxBillId(String taxBillId) {
		this.taxBillId = taxBillId;
	}

	public String getProcessFlowId() {
		return processFlowId;
	}

	public void setProcessFlowId(String processFlowId) {
		this.processFlowId = processFlowId;
	}

	public String getTaxRoleId() {
		return taxRoleId;
	}

	public void setTaxRoleId(String taxRoleId) {
		this.taxRoleId = taxRoleId;
	}

	public Date getBillStartDate() {
		return billStartDate;
	}

	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}

	public Date getBillEndDate() {
		return billEndDate;
	}

	public void setBillEndDate(Date billEndDate) {
		this.billEndDate = billEndDate;
	}

	@Override
	public void appendContents(StringBuilder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNull() {
		// TODO Auto-generated method stub
		return false;
	}

}
