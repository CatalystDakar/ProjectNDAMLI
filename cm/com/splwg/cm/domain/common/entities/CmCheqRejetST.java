package com.splwg.cm.domain.common.entities;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Id;
import com.splwg.base.api.datatypes.Money;

@SuppressWarnings("serial")
public class CmCheqRejetST implements Id {
	
	private String rejetPaiementId;
	private String exttransId;
	private String fileId;
	private String fileName;
	private Date fileDate;
	private Date transDate;
	private Date processDate;
	private String extSourceId;
	private BigInteger cheqRjtDtlSeq;
	private String cheqRjtStFlg;
	private String cancelReason;
	private String tenderTypeCD;
	private String checkNbr;
	private String currentCD;
	private Money tenderAmt;
	private String fraisRjtSW;
	private String fraisRsn;
	private Money fraisRjtMT;
	private Date accountingDate;
	private String microId;
	private String custId;
	private String name1;
	private String extReferenceId;
	private String tenderCtrlId;
	private String acctId;
	private String payEventId;
	private String payTenderId;
	private BigInteger msgCatNumber;
	private BigInteger msgNumber;
	private String mesText;
	
	public CmCheqRejetST() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CmCheqRejetST(String rejetPaiementId, String exttransId, String fileId, String fileName, Date fileDate,
			Date transDate, Date processDate, String extSourceId, BigInteger cheqRjtDtlSeq, String cheqRjtStFlg,
			String cancelReason, String tenderTypeCD, String checkNbr, String currentCD, Money tenderAmt,
			String fraisRjtSW, String fraisRsn, Money fraisRjtMT, Date accountingDate, String microId, String custId,
			String name1, String extReferenceId, String tenderCtrlId, String acctId, String payEventId,
			String payTenderId, BigInteger msgCatNumber, BigInteger msgNumber, String mesText) {
		super();
		this.rejetPaiementId = rejetPaiementId;
		this.exttransId = exttransId;
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileDate = fileDate;
		this.transDate = transDate;
		this.processDate = processDate;
		this.extSourceId = extSourceId;
		this.cheqRjtDtlSeq = cheqRjtDtlSeq;
		this.cheqRjtStFlg = cheqRjtStFlg;
		this.cancelReason = cancelReason;
		this.tenderTypeCD = tenderTypeCD;
		this.checkNbr = checkNbr;
		this.currentCD = currentCD;
		this.tenderAmt = tenderAmt;
		this.fraisRjtSW = fraisRjtSW;
		this.fraisRsn = fraisRsn;
		this.fraisRjtMT = fraisRjtMT;
		this.accountingDate = accountingDate;
		this.microId = microId;
		this.custId = custId;
		this.name1 = name1;
		this.extReferenceId = extReferenceId;
		this.tenderCtrlId = tenderCtrlId;
		this.acctId = acctId;
		this.payEventId = payEventId;
		this.payTenderId = payTenderId;
		this.msgCatNumber = msgCatNumber;
		this.msgNumber = msgNumber;
		this.mesText = mesText;
	}

	public String getRejetPaiementId() {
		return rejetPaiementId;
	}

	public void setRejetPaiementId(String rejetPaiementId) {
		this.rejetPaiementId = rejetPaiementId;
	}

	public String getExttransId() {
		return exttransId;
	}

	public void setExttransId(String exttransId) {
		this.exttransId = exttransId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getFileDate() {
		return fileDate;
	}

	public void setFileDate(Date fileDate) {
		this.fileDate = fileDate;
	}

	public Date getTransDate() {
		return transDate;
	}

	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}

	public Date getProcessDate() {
		return processDate;
	}

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}

	public String getExtSourceId() {
		return extSourceId;
	}

	public void setExtSourceId(String extSourceId) {
		this.extSourceId = extSourceId;
	}

	public BigInteger getCheqRjtDtlSeq() {
		return cheqRjtDtlSeq;
	}

	public void setCheqRjtDtlSeq(BigInteger cheqRjtDtlSeq) {
		this.cheqRjtDtlSeq = cheqRjtDtlSeq;
	}

	public String getCheqRjtStFlg() {
		return cheqRjtStFlg;
	}

	public void setCheqRjtStFlg(String cheqRjtStFlg) {
		this.cheqRjtStFlg = cheqRjtStFlg;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public String getTenderTypeCD() {
		return tenderTypeCD;
	}

	public void setTenderTypeCD(String tenderTypeCD) {
		this.tenderTypeCD = tenderTypeCD;
	}

	public String getCheckNbr() {
		return checkNbr;
	}

	public void setCheckNbr(String checkNbr) {
		this.checkNbr = checkNbr;
	}

	public String getCurrentCD() {
		return currentCD;
	}

	public void setCurrentCD(String currentCD) {
		this.currentCD = currentCD;
	}

	public Money getTenderAmt() {
		return tenderAmt;
	}

	public void setTenderAmt(Money tenderAmt) {
		this.tenderAmt = tenderAmt;
	}

	public String getFraisRjtSW() {
		return fraisRjtSW;
	}

	public void setFraisRjtSW(String fraisRjtSW) {
		this.fraisRjtSW = fraisRjtSW;
	}

	public String getFraisRsn() {
		return fraisRsn;
	}

	public void setFraisRsn(String fraisRsn) {
		this.fraisRsn = fraisRsn;
	}

	public Money getFraisRjtMT() {
		return fraisRjtMT;
	}

	public void setFraisRjtMT(Money fraisRjtMT) {
		this.fraisRjtMT = fraisRjtMT;
	}

	public Date getAccountingDate() {
		return accountingDate;
	}

	public void setAccountingDate(Date accountingDate) {
		this.accountingDate = accountingDate;
	}

	public String getMicroId() {
		return microId;
	}

	public void setMicroId(String microId) {
		this.microId = microId;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public String getExtReferenceId() {
		return extReferenceId;
	}

	public void setExtReferenceId(String extReferenceId) {
		this.extReferenceId = extReferenceId;
	}

	public String getTenderCtrlId() {
		return tenderCtrlId;
	}

	public void setTenderCtrlId(String tenderCtrlId) {
		this.tenderCtrlId = tenderCtrlId;
	}

	public String getAcctId() {
		return acctId;
	}

	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}

	public String getPayEventId() {
		return payEventId;
	}

	public void setPayEventId(String payEventId) {
		this.payEventId = payEventId;
	}

	public String getPayTenderId() {
		return payTenderId;
	}

	public void setPayTenderId(String payTenderId) {
		this.payTenderId = payTenderId;
	}

	public BigInteger getMsgCatNumber() {
		return msgCatNumber;
	}

	public void setMsgCatNumber(BigInteger msgCatNumber) {
		this.msgCatNumber = msgCatNumber;
	}

	public BigInteger getMsgNumber() {
		return msgNumber;
	}

	public void setMsgNumber(BigInteger msgNumber) {
		this.msgNumber = msgNumber;
	}

	public String getMesText() {
		return mesText;
	}

	public void setMesText(String mesText) {
		this.mesText = mesText;
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
