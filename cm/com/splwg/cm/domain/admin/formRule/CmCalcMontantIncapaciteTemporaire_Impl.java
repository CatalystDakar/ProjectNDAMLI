package com.splwg.cm.domain.admin.formRule;

import org.joda.time.Days;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;

/**
 * @author Papa
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = salaireJournalierMoyen, required = true, type = decimal)
 *            , @AlgorithmSoftParameter (name = nombreJoursOfFirstPayment, required = true, type = decimal)
 *            , @AlgorithmSoftParameter (name = periodiciteDePaiement, required = true, type = decimal)})
 */
public class CmCalcMontantIncapaciteTemporaire_Impl extends CmCalcMontantIncapaciteTemporaire_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {
	
	private BusinessObjectInstanceKey boKey;
	private BusinessObjectInstance boInstance;
	private final static Logger log = LoggerFactory.getLogger(CmEmployeeRegAlgo_Impl.class);

	@Override
	public void invoke() {
		// TODO Auto-generated method stub


		 System.out.println("I am In Invoke method " + this.boKey);
		 log.info("I am In Invoke method BO intance Key " + this.boKey); 
		 this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		 log.info("I am In Invoke method BO intance " + this.boInstance);  
		 
		 //CHAMPS
		 Date dateAT =(Date) this.boInstance.getFieldAndMDForPath("champs/dateAT").getValue();
		 BigDecimal nbreJoursRepos =(BigDecimal) this.boInstance.getFieldAndMDForPath("champs/nbreJoursRepos").getValue();
		 BigDecimal salaireDernierMois =(BigDecimal) this.boInstance.getFieldAndMDForPath("champs/salaireDernierMois").getValue();
		 BigDecimal nbreJoursTrav =(BigDecimal) this.boInstance.getFieldAndMDForPath("champs/nbreJoursTrav").getValue();
		 BigDecimal salaireJournalier =(BigDecimal) this.boInstance.getFieldAndMDForPath("champs/salaireJournalier").getValue();
		 BigDecimal nbreHeurSalJourn =(BigDecimal) this.boInstance.getFieldAndMDForPath("champs/nbreHeurSalJourn").getValue();
		 Date dateOuverture=getSystemDateTime().getDate();
		 
		// int days = Days.daysBetween(date1, date2).getDays();

		 
		 //DOCUMENTS
		 Bool cni =(Bool) this.boInstance.getFieldAndMDForPath("document/cni").getValue();
		 Bool declarationAT =(Bool) this.boInstance.getFieldAndMDForPath("document/declarationAT").getValue();
		 Bool questionTrajet =(Bool) this.boInstance.getFieldAndMDForPath("document/questionTrajet").getValue();
		 Bool certifMedical =(Bool) this.boInstance.getFieldAndMDForPath("document/certifMedical").getValue();
		 Bool dernierBS =(Bool) this.boInstance.getFieldAndMDForPath("document/dernierBS").getValue();
		 Bool bulletinSalJournalier =(Bool) this.boInstance.getFieldAndMDForPath("document/bulletinSalJournalier").getValue();
		 Bool rapportDeMer =(Bool) this.boInstance.getFieldAndMDForPath("document/rapportDeMer").getValue();
		 
		BigDecimal salaireMoyen=new BigDecimal(0); 
		
		BigDecimal montantApayer=new BigDecimal(0);
		 
		 if((salaireDernierMois!=null) && (nbreJoursTrav!=null)){
			 salaireMoyen=salaireDernierMois.divide(nbreJoursTrav);  
		 }
		 else{
			 salaireMoyen=salaireJournalier.multiply(nbreHeurSalJourn).divide(new BigDecimal(8));     
		 }
		  
		 if(salaireMoyen.compareTo(getSalaireJournalierMoyen())==1){
			 salaireMoyen=getSalaireJournalierMoyen();
		 }
		 
		 if(nbreJoursRepos.compareTo(getNombreJoursOfFirstPayment())==-1 || nbreJoursRepos.compareTo(getNombreJoursOfFirstPayment())==0){
			montantApayer=salaireMoyen.divide(new BigDecimal(2)).multiply(nbreJoursRepos); 
			
		 }
		 else{
			 montantApayer=salaireMoyen.divide(new BigDecimal(2)).multiply(getNombreJoursOfFirstPayment()).add(salaireMoyen.divide(new BigDecimal(2/3)).multiply(nbreJoursRepos.subtract(getNombreJoursOfFirstPayment())));
			 
			 
		 }
		 
		
		 
	}


	@Override
	public boolean getForcePostProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		// TODO Auto-generated method stub
		
	}

}
