package com.splwg.cm.domain.batch;

public class CmGlConstant {
	
	public static final String IPRES_LABEL="IPRES_Livre_Comptable";
	public static final String CSS_LABEL="CSS_Livre_Comptable";
	
	public static final String ACTUAL_FLAG="A";
	public static final String ENTERED_DR="XOF";
	public static final String ENTERED_CR="XOF";
	
	public static final String ACCOUNT_TYPE_QUERY="SELECT ACCT.CUST_CL_CD FROM CI_ACCT ACCt WHERE ACCT.ACCT_ID=(SELECT distinct PAY.ACCT_ID FROM CI_PAY PAY WHERE PAY.PAY_ID in(SELECT SEG.PAY_ID FROM CI_PAY_SEG SEG,CI_FT FT WHERE FT.SA_ID = SEG.SA_ID AND FT.FT_ID =:FTID)) and rownum<=1";
    public static final String PERIOD_DESC_QUERY="select distinct(pl.period_descr) from CI_CAL_PERIOD_L pl where pl.fiscal_year=(select p.fiscal_year from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID)and pl.accounting_period= (select p.accounting_period  from CI_CAL_PERIOD p,ci_ft ft where ft.accounting_dt between p.begin_dt and p.end_Dt and ft.ft_id=:FTID) AND pl.language_cd='FRA'";
    public static final String BANK_ACCOUNT_NAME_QUERY="select distinct g.descr from ci_ft a, ci_pay b, ci_pay_tndr c, ci_tndr_ctl d, ci_tndr_srce e, ci_bank_account f,ci_bank_account_l g where f.bank_acct_key = g.bank_acct_key and g.language_cd='FRA' and a.ft_type_flg in('PS','PX') and a.parent_id=b.pay_id and b.pay_event_id=c.pay_event_id and c.tndr_ctl_id =d.tndr_ctl_id and d.tndr_source_cd=e.tndr_source_cd and e.bank_acct_key=f.bank_acct_key and a.ft_id =:FTID";
    public static final String TENDER_TYPE_QUERY="select a.tender_type_cd from ci_pay_tndr a, ci_pay b, ci_pay_seg c,ci_ft d where a.pay_event_id = b.pay_event_id  and b.pay_id = c.pay_id and d.sa_id = c.sa_id and d.ft_id=:FTID and c.pay_id=d.parent_id";
    public static final String REJECT_CHEQUE_NBR_QUERY="select a.check_nbr from ci_pay_tndr a, ci_pay b, ci_pay_seg c,ci_ft d where a.pay_event_id = b.pay_event_id and  a.tender_type_cd='CHEC' and a.tndr_status_flg='60' and b.pay_id = c.pay_id and d.sa_id= c.sa_id and  d.ft_id=:FTID and c.pay_id = d.parent_id";
    public static final String TENDER_ID_QUERY="select a.pay_tender_id from ci_pay_tndr a, ci_pay b,ci_pay_seg c,ci_ft d where a.pay_event_id = b.pay_event_id and b.pay_id = c.pay_id and c.pay_seg_id = d.sibling_id and d.ft_id=:FTID";
    public static final String PRESTATION_TYPE_QUERY="select WFM_OPT_VAL from CI_WFM_OPT where seq_num=(select seq_num from CI_WFM_OPT where WFM_NAME=:wfm_name and EXT_OPT_TYPE='CPR' and WFM_OPT_VAL=:wfm_val) and EXT_OPT_TYPE='NOP'";
    
	public static final String CUST_CL_CD="CUST_CL_CD";
	public static final String PERIOD_DESCR="PERIOD_DESCR";
	public static final String DESCR="DESCR";
	public static final String TENDER_TYPE_CD="TENDER_TYPE_CD";
	public static final String TENDER_ID="PAY_TENDER_ID";
	public static final String CHECK_NBR="CHECK_NBR";
    public static final String PAY_TENDER_ID="PAY_TENDER_ID";
    public static final String WFM_OPT_VAL="WFM_OPT_VAL";
    
   
	public static final String BRN1_OPTION="BRN1";
    public static final String SIT1_OPTION="SIT1";
    public static final String CODE_JOURNAL1="CJC1";
    public static final String BRN2_OPTION="BRN2";
    public static final String SIT2_OPTION="SIT2";
    public static final String CODE_JOURNAL2="CJC2";
    public static final String BRN3_OPTION="BRN3";
    public static final String SIT3_OPTION="SIT3";
    public static final String CODE_JOURNAL3="CJC3";
    public static final String EXT_OPT_TYPE="NOC";
    public static final String CURRENCY_CD="XOF";
    
}
