package com.splwg.cm.domain.batch;

public class CmGlConstant {
	
	public static final String IPRES_LABEL="IPRES_Livre_Comptable";
	public static final String CSS_LABEL="CSS_Livre_Comptable";
	
	public static final String ACTUAL_FLAG="A";
	
	public static final String ENTERED_DR="XOF";
	public static final String ENTERED_CR="XOF";
	
	public static final String ACCOUNT_TYPE_QUERY="SELECT ACCT.CUST_CL_CD FROM CI_ACCT ACCt WHERE ACCT.ACCT_ID=(SELECT distinct PAY.ACCT_ID FROM CI_PAY PAY WHERE PAY.PAY_ID in(SELECT SEG.PAY_ID FROM CI_PAY_SEG SEG,CI_FT FT WHERE FT.SA_ID = SEG.SA_ID AND FT.FT_ID =:FTID)) and rownum<=1";
    public static final String PERIOD_DESC_QUERY="select distinct(pl.period_descr) from CI_CAL_PERIOD_L pl where pl.fiscal_year=(select p.fiscal_year from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID)and pl.accounting_period= (select p.accounting_period  from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID) AND pl.language_cd='FRA'";
    public static final String BANK_ACCOUNT_NBR_QUERY="select cib.account_nbr from ci_bank_account cib where cib.bank_acct_key=(select src.bank_acct_key from ci_tndr_srce src where src.tndr_source_cd=(select ctl.tndr_source_cd from ci_tndr_ctl ctl where ctl.tndr_ctl_id=(select distinct cpt.tndr_ctl_id from ci_pay_tndr cpt where cpt.pay_event_id=(select  pay.pay_event_id from ci_pay pay where pay.pay_id=(select parent_id from ci_ft where ft_id=:FTID)))))";
    public static final String TENDER_TYPE_QUERY="SELECT PAYT.TENDER_TYPE_CD FROM CI_PAY_TNDR PAYT WHERE PAYT.PAY_EVENT_ID=(SELECT PAY.PAY_EVENT_ID FROM CI_PAY PAY WHERE PAY.PAY_ID =(SELECT SEG.PAY_ID FROM CI_PAY_SEG SEG,CI_FT FT WHERE FT.SA_ID = SEG.SA_ID AND FT.FT_ID =:FTID and seg.pay_id=FT.PARENT_ID))";
    public static final String REJECT_CHEQUE_NBR_QUERY="select cib.account_nbr from ci_bank_account cib where cib.bank_acct_key=(select src.bank_acct_key from ci_tndr_srce src where src.tndr_source_cd=(select ctl.tndr_source_cd from ci_tndr_ctl ctl where ctl.tndr_ctl_id=(select cpt.tndr_ctl_id from ci_pay_tndr cpt where cpt.pay_event_id=(select  pay.pay_event_id from ci_pay pay where pay.pay_id=(select parent_id from ci_ft where ft_id=:FTID)))))";
    public static final String TENDER_ID_QUERY="select pay_tender_id from CI_PAY_TNDR where pay_event_id=(select pay_id from ci_pay_seg where pay_seg_id=(select sibling_id from ci_ft where ft_id=:FTID))";
    public static final String PRESTATION_TYPE_QUERY="select wfm_opt_val from CI_WFM_OPT where seq_num=(select seq_num from CI_WFM_OPT where WFM_OPT_VAL=: and EXT_OPT_TYPE='CPR') and EXT_OPT_TYPE='NOP'";
    
	public static final String CUST_CL_CD="CUST_CL_CD";
	public static final String PERIOD_DESCR="PERIOD_DESCR";
	public static final String ACCOUNT_NBR="ACCOUNT_NBR";
	public static final String TENDER_TYPE_CD="TENDER_TYPE_CD";
	public static final String TENDER_ID="PAY_TENDER_ID";
	public static final String CHECK_NBR="CHECK_NBR";
    public static final String PAY_TENDER_ID="PAY_TENDER_ID";
    public static final String WFM_OPT_VAL="WFM_OPT_VAL";
}
