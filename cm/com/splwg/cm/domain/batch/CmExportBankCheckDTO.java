package com.splwg.cm.domain.batch;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;

public class CmExportBankCheckDTO {

	private Date creDttm;
	private String batchNbr;
	private String depCtrlId;
	private String tndrSrceCd;
	private String banqDescr;
	private String accntNbr;
	private String bankCd;
	private String bankAcctKey;
	private Date balanceDttm;
	private String tndrCtrlId;
	private String payEvntId;
	private String payTndrId;
	private String payorAccntId;
	private Date payDate;
	private String checkNbr;
	private String currencyCd;
	private String banqCheq;
	private String rsCheq;
	private String payId;
	//private String paySegId;
	//private String ftId;
	//private String glDistribStatus;
	//private String cgDebit;
	//private String cgCredit;
	private String tenderAmount;
	///private String cgDebtAmnt;
	//private String cgCreditAmnt;


	public Date getCreDttm() {
		return creDttm;
	}
	public void setCreDttm(Date creDttm) {
		this.creDttm = creDttm;
	}
	public String getBatchNbr() {
		return batchNbr;
	}
	public void setBatchNbr(String batchNbr) {
		this.batchNbr = batchNbr;
	}
	public String getDepCtrlId() {
		return depCtrlId;
	}
	public void setDepCtrlId(String depCtrlId) {
		this.depCtrlId = depCtrlId;
	}
	public String getTndrSrceCd() {
		return tndrSrceCd;
	}
	public void setTndrSrceCd(String tndrSrceCd) {
		this.tndrSrceCd = tndrSrceCd;
	}
	public String getBanqDescr() {
		return banqDescr;
	}
	public void setBanqDescr(String banqDescr) {
		this.banqDescr = banqDescr;
	}
	public String getAccntNbr() {
		return accntNbr;
	}
	public void setAccntNbr(String accntNbr) {
		this.accntNbr = accntNbr;
	}
	public String getBankCd() {
		return bankCd;
	}
	public void setBankCd(String bankCd) {
		this.bankCd = bankCd;
	}
	public String getBankAcctKey() {
		return bankAcctKey;
	}
	public void setBankAcctKey(String bankAcctKey) {
		this.bankAcctKey = bankAcctKey;
	}
	public Date getBalanceDttm() {
		return balanceDttm;
	}
	public void setBalanceDttm(Date balanceDttm) {
		this.balanceDttm = balanceDttm;
	}
	public String getTndrCtrlId() {
		return tndrCtrlId;
	}
	public void setTndrCtrlId(String tndrCtrlId) {
		this.tndrCtrlId = tndrCtrlId;
	}
	public String getPayEvntId() {
		return payEvntId;
	}
	public void setPayEvntId(String payEvntId) {
		this.payEvntId = payEvntId;
	}
	public String getPayTndrId() {
		return payTndrId;
	}
	public void setPayTndrId(String payTndrId) {
		this.payTndrId = payTndrId;
	}
	public String getPayorAccntId() {
		return payorAccntId;
	}
	public void setPayorAccntId(String payorAccntId) {
		this.payorAccntId = payorAccntId;
	}
	public Date getPayDate() {
		return payDate;
	}
	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}
	public String getCheckNbr() {
		return checkNbr;
	}
	public void setCheckNbr(String checkNbr) {
		this.checkNbr = checkNbr;
	}
	public String getCurrencyCd() {
		return currencyCd;
	}
	public void setCurrencyCd(String currencyCd) {
		this.currencyCd = currencyCd;
	}
	public String getBanqCheq() {
		return banqCheq;
	}
	public void setBanqCheq(String banqCheq) {
		this.banqCheq = banqCheq;
	}
	public String getRsCheq() {
		return rsCheq;
	}
	public void setRsCheq(String rsCheq) {
		this.rsCheq = rsCheq;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getTenderAmount() {
		return tenderAmount;
	}
	public void setTenderAmount(String tenderAmount) {
		this.tenderAmount = tenderAmount;
	}
	
}
