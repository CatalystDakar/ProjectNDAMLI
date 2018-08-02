package com.splwg.cm.domain.admin.formRule;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Date;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import java.util.Iterator;

/**
 * @author Catalyst-Demo
 *
@AlgorithmComponent ()
 */
public class CmCheckNumberEmployees_Impl extends CmCheckNumberEmployees_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	Logger logger = LoggerFactory.getLogger(CmCheckEmployeesExistence_Impl.class);
	
	public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData) {
		this.inputData = applyFormRuleAlgorithmInputData;
	}

	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData) {
		this.inputOutputData = applyFormRuleAlgorithmInputOutputData;
	}

	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return this.inputOutputData;
	}

	@Override
	public void invoke() {
		
		BusinessObjectInstance formBusinessObjectInstance = (BusinessObjectInstance) this.inputOutputData
				.getFormBusinessObject();
		COTSInstanceNode groupInsInfosSalariedNode=formBusinessObjectInstance.getGroup("informationSalaries");
		COTSInstanceList cOTSInstanceListSalarieds=groupInsInfosSalariedNode.getList("informationSalariesList");
		BigDecimal totalSalSectSalarieds=BigDecimal.valueOf(cOTSInstanceListSalarieds.getSize());
		
		COTSInstanceNode groupSyntheseNode=formBusinessObjectInstance.getGroup("synthese");
		COTSInstanceNode groupTotalSalariedNode=groupSyntheseNode.getGroup("totalSalaries");
		BigDecimal totalSalSectSynthese=groupTotalSalariedNode.getNumber("asCurrent");
		
		COTSInstanceNode groupInformationEmployeurNode=formBusinessObjectInstance.getGroup("informationEmployeur");
		COTSInstanceNode groupDateDebutCotisationdNode=groupInformationEmployeurNode.getGroup("dateDebutCotisation");
		Date dateDebutCotisation=groupDateDebutCotisationdNode.getDate("asCurrent");
	
		if( !totalSalSectSynthese.equals(totalSalSectSalarieds)){
			addError(CmMessageRepository90000.MSG_10004(totalSalSectSynthese.toString(), totalSalSectSalarieds.toString()));
		}
		
		verifyDateSortieEmployee(cOTSInstanceListSalarieds, dateDebutCotisation);
	}
	/**
	 * Verifier si la date sortie salarié est supérieur à la date de début de la période de cotisation
	 * @param cOTSInstanceListSalarieds
	 * @param dateDebutCotisation
	 */
	public void verifyDateSortieEmployee(COTSInstanceList cOTSInstanceListSalarieds, Date dateDebutCotisation ){
		Iterator<COTSInstanceListNode> listInstanceNodeIterator=  cOTSInstanceListSalarieds.iterator();
		
		
		while(listInstanceNodeIterator.hasNext()){
			COTSInstanceListNode cOTSInstanceListSalariedNode=listInstanceNodeIterator.next();
			COTSInstanceNode cotsDateSortieGroup=cOTSInstanceListSalariedNode.getGroup("dateSortie");
			Date dateSortieCotisation=cotsDateSortieGroup.getDate("asCurrent");

			COTSInstanceNode cotsInsPrenomGroup=cOTSInstanceListSalariedNode.getGroup("prenom");
			String prenom=cotsInsPrenomGroup.getFieldAndMD("asCurrent").getXMLValue();
			COTSInstanceNode cotsInsNomGroup=cOTSInstanceListSalariedNode.getGroup("nom");
			String nom=cotsInsNomGroup.getFieldAndMD("asCurrent").getXMLValue();
			
			if(notNull(dateSortieCotisation))
				if(dateDebutCotisation.isAfter(dateSortieCotisation)){
					addError(CmMessageRepository90000.MSG_10005(prenom, nom, dateDebutCotisation.toString()));
				}
		}
	}
}
