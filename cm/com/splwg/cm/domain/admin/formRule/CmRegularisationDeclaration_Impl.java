package com.splwg.cm.domain.admin.formRule;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;

/**
 * @author Khadim Cisse
 *
@AlgorithmComponent ()
 */
public class CmRegularisationDeclaration_Impl extends CmRegularisationDeclaration_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	Logger logger = LoggerFactory.getLogger(CmCreateObligation_Impl.class);

	@Override
	public void invoke() {
		// TODO Auto-generated method stub
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		String adjustedFromFormID = formBoInstance.getString("adjustedFromForm");
		String idFormulaire = formBoInstance.getString("taxFormId");
		
	    if(notNull(adjustedFromFormID)) {
	    
		    String getAdjustementQuery = "select adj.adj_id,adj_type_cd,adj_amt, adj.sa_id "
					+ "from ci_adj_char car inner join ci_adj adj on car.adj_id = adj.adj_id "
					+ "where char_type_cd = 'ID_DNS' and char_val_fk1 =:formId";
			
			PreparedStatement preparedStatement = createPreparedStatement(getAdjustementQuery, "SELECT");
			preparedStatement.bindString("formId", adjustedFromFormID , null);
			//SQLResultRow sqlResultRow = preparedStatement.firstRow();
			QueryIterator<SQLResultRow> SQLResultRowList =  preparedStatement.iterate();
			
			Date dateDebutCotisation = (Date) formBoInstance
					.getFieldAndMDForPath("informationEmployeur/dateDebutCotisation/asCurrent").getValue();
	
			while(SQLResultRowList.hasNext()) {
				SQLResultRow sqlResultRow = SQLResultRowList.next();
				//String ADJ_ID = sqlResultRow.getString(ADJUSTEMENT.ADJ_ID.name());
				String ADJ_TYPE_CD = sqlResultRow.getString(ADJUSTEMENT.ADJ_TYPE_CD.name());
				Money ADJ_AMT = sqlResultRow.getMoney(ADJUSTEMENT.ADJ_AMT.name());
				String SA_ID = sqlResultRow.getString(ADJUSTEMENT.SA_ID.name());
				
				String ADJ_TYPE_CD_REG = getAdjustementTypeRegularisation(ADJ_TYPE_CD);
				Money MNT_COT_AMEND = getMontantCotisation(formBoInstance, ADJ_TYPE_CD);
				Money ADJ_AMT_REG = getMontantRegularisation(ADJ_AMT, MNT_COT_AMEND);
				if (ADJ_AMT_REG != null && !ADJ_AMT_REG.isZero()) {
					String idAdjustment = createAjustementBS(ADJ_TYPE_CD_REG, SA_ID, ADJ_AMT_REG.getAmount(), dateDebutCotisation);
					ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
					String idGroupeFT=getFtIdByAdjId(idAdjustment);
					updateGroupFtId(idGroupeFT); 
				}
			}
			
	    }
	}
	
	
	
	
	private String createAjustementBS(String adjustType, String obligationId, BigDecimal adjustmentAmount, Date effectiveDate) {
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-AdjustmentAddFreeze");
		if (null != adjustType && null != obligationId && null != adjustmentAmount) {
			COTSInstanceNode group = bsInstance.getGroupFromPath("input");
			group.set("serviceAgreement", obligationId);
			group.set("adjustmentType", adjustType);
			group.set("adjustmentAmount", adjustmentAmount);
			System.out.println(getSystemDateTime().getDate());
			group.set("adjustmentDate", getSystemDateTime().getDate());// getSystemDateTime().getDate()
			group.set("arrearsDate", effectiveDate);
		}
		// Execute BS and return the Ninea if exists
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceNode output = bsInstance.getGroupFromPath("output");
		return output.getString("adjustment");

	}
	
	private Money getMontantRegularisation(Money montantFormOriginal, Money montantFormAmend) {
		
		return montantFormAmend.subtract(montantFormOriginal);
	}
	
	
	private String getAdjustementTypeRegularisation(String ADJ_TYPE_CD) {

		ADJ_TYPE_CD = ADJ_TYPE_CD.replaceAll("\\s","");
		String AdjustementTypeRegularisation = null;
		
		switch (ADJ_TYPE_CD) {
			
			case "CPF" : AdjustementTypeRegularisation = "CM-RPF";
				break;
				
			case "CATMP" : AdjustementTypeRegularisation = "CM-RATMP";	
				 break;
				
			case "CR" : AdjustementTypeRegularisation = "CM-RR";
				 break;
			
		}
		
		return AdjustementTypeRegularisation;
	}
	
	public enum ADJUSTEMENT {
		ADJ_ID,
		ADJ_TYPE_CD,
		ADJ_AMT,
		SA_ID
	}
	
	
	private Money getMontantCotisation(BusinessObjectInstance formBoInstance, String branche){

		branche = branche.replaceAll("\\s","");

		if (branche.equalsIgnoreCase("CPF"))
			return  (Money) formBoInstance.getFieldAndMDForPath("synthese/montantPF/asCurrent").getValue();
		if (branche.equalsIgnoreCase("CATMP"))
			return(Money) formBoInstance.getFieldAndMDForPath("synthese/montantATMP/asCurrent").getValue();
		Money montantRRG = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantRRG/asCurrent").getValue();
		Money montantRCC = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantRCC/asCurrent").getValue();
		if (branche.equalsIgnoreCase("CR"))
			return montantRRG.add(montantRCC);
		
		return null;
	}
	
	private String getFtIdByAdjId(String adjId){
		String resultat=null;
		String query = "SELECT * FROM CI_FT WHERE SIBLING_ID=:adjIdSoft  AND FT_TYPE_FLG = 'AD'";
		PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");
		preparedStatement.bindString("adjIdSoft", adjId, null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		if (sqlResultRow != null) {
			resultat=sqlResultRow.getString("FT_ID");
		}
	
		return resultat; 
		
	}
	
	private void updateGroupFtId(String ftId){
		String query = "UPDATE CI_FT SET GRP_FT_ID=:ftIdSoft1 WHERE FT_ID=:ftIdSoft2";
		PreparedStatement preparedStatement = createPreparedStatement(query, "UPDATE");
		preparedStatement.bindString("ftIdSoft1", ftId, null);
		preparedStatement.bindString("ftIdSoft2", ftId, null);
		preparedStatement.executeUpdate();
		System.out.println("MISA JOUR REUSSI");
		
	}
	
	private void ajouterDNSAdj(String adjId, String valFk1, String srchval) {
		String query = "INSERT INTO CI_ADJ_CHAR(ADJ_ID, CHAR_VAL_FK1, SRCH_CHAR_VAL, CHAR_TYPE_CD, SEQ_NUM) VALUES(:adjIdSoft, :valFk1Soft, :srchvalSoft, 'ID_DNS' ,1)";
		PreparedStatement preparedStatement = createPreparedStatement(query, "INSERT");
		preparedStatement.bindString("adjIdSoft", adjId, null);
		preparedStatement.bindString("valFk1Soft", valFk1, null);
		preparedStatement.bindString("srchvalSoft", srchval, null);
		preparedStatement.executeUpdate();

	}


	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return this.inputOutputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		this.inputData = applyFormRuleAlgorithmInputData;

	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}
	
	

}
