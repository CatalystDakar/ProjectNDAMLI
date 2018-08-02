package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.BusinessEntity;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.MaintenanceObjectInfo;
import com.splwg.base.support.schema.MaintenanceObjectInfoCache;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.tax.domain.admin.filingCalendar.FilingCalendar_Id;
import com.splwg.tax.domain.admin.filingCalendar.FilingPeriod;
import com.splwg.tax.domain.admin.filingCalendar.FilingPeriod_Id;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.formRule.FormRule_Id;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormTypeObligationType;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.tax.domain.customerinfo.account.Account;
import com.splwg.tax.domain.customerinfo.account.Account_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.tax.domain.customerinfo.taxRole.TaxRole;
import com.splwg.tax.domain.customerinfo.taxRole.TaxRole_Id;

/**
 * @author Khadim Cissé
 *
@AlgorithmComponent ()
 */
public class CmCheckFilingObligationsExistence_Impl extends CmCheckFilingObligationsExistence_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	private BusinessObjectInstance ruleBoInstance;
	Logger logger = LoggerFactory.getLogger(CmCheckFilingObligationsExistence_Impl.class);
	
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
		//Type Identifiant employeur
		String idType=formBusinessObjectInstance.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getXMLValue();
		//identifiant employeur
		String idNumber = (String) formBusinessObjectInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent").getValue();
		//Date de debut de la periode de cotisation
		Date dateDebutCotisation = (Date) formBusinessObjectInstance
				.getFieldAndMDForPath("informationEmployeur/dateDebutCotisation/asCurrent").getValue();
		//Date fin de la periode de cotisation
		Date dateFinCotisation = (Date) formBusinessObjectInstance
				.getFieldAndMDForPath("informationEmployeur/dateFinCotisation/asCurrent").getValue();
		
		//Recuperation des information du BO du form Rule
		BusinessObjectInstance formRuleBoInstance = this.getRuleDetails();
		// Recuperation du facteur NBR_SAL au niveau du form Rule
		String factorNbreSal = formRuleBoInstance.getString("nbrSal");
		String nombreSal = getFactorVal(factorNbreSal, dateDebutCotisation.toString(), dateFinCotisation.toString());
		BigDecimal nombreSalToBD=new BigDecimal(nombreSal);
		
		//Nombre de salaries declares dans le formulaire DNS
		int nombreSalaries = 0;
		BigDecimal nombreEmployes = (BigDecimal) formBusinessObjectInstance.getFieldAndMDForPath("synthese/totalSalaries/asCurrent").getValue();//getNombreEmployes(idEmployeur);  
		if (nombreEmployes != null) {
			nombreSalaries = nombreEmployes.intValue();
			logger.info("nombreSalaries" +nombreSalaries);    
		}
		
		//Recuperation de la liste des Obligation Type configurer dans le Form Type
		Iterator<FormTypeObligationType> iteratorFormTypeObligationType=this.inputData.getFormTypeId().getEntity().getObligationTypes().iterator();
		while (iteratorFormTypeObligationType.hasNext()) {
			
			FormTypeObligationType formTypeObligType = iteratorFormTypeObligationType.next();
			//Obligation Type definit dans le form Type
			ServiceAgreementType obligationType = formTypeObligType.fetchIdServiceAgreementType();
			//Identifiant du calendrier
			FilingCalendar_Id filingCalendarId=null;
			/**
			 * Si le nombre de salarie declarer dans le formulaire est superieur au nombre
			 * de salaries configurer dans le facteur NBR_SAL on utilise le calendrier 
			 * Mensuel: SU-MONTHLY
			 * Sinon on utilise le calendrier trimestriel:SU-QUARTER
			 */
			if(nombreSalaries>=nombreSalToBD.intValue()){
				filingCalendarId = new FilingCalendar_Id("SU-MONTHLY");
			}else{
				filingCalendarId = new FilingCalendar_Id("SU-QUARTER");
			}

			//Date fin cotisation dans le formulaire DNS
			Date filingPeriodEndDate = formBusinessObjectInstance.getGroup("informationEmployeur").getGroup("dateFinCotisation").getDate("asCurrent");
			FilingPeriod_Id filingPeriodId = new FilingPeriod_Id(filingCalendarId, filingPeriodEndDate);
			FilingPeriod filingPeriod = (FilingPeriod) filingPeriodId.getEntity();
			if (!this.isNull(filingPeriod)) {
				//Recuperation employeur
				Person employer= getPersonByIdNumber(idType,idNumber);
				//Recuperation des comptes de l'employeur
				List<Account> listAccount= getAccountsByIdPerson(employer.getId().getTrimmedValue());
				for(Account account:listAccount){
					if (!this.isNull(account)) {
						//Pour chaque compte on recuperere le Tax Role associe
						TaxRole taxRole=getTagRoleByAccountId(account.getId().getTrimmedValue());
						if (!obligationType.getServiceType().getTaxRoleApplicability().isRequired() || !this.isNull(taxRole)) {
							/**
							 * Rechercher s'il existe des Obligations de meme type que ceux que nous avons au niveau Form Type
							 * Dans la meme Periode de declaration. 
							 */
							Query query = this.retrieveObligations(account, obligationType, filingPeriod, taxRole);
							if (query.listSize() > 0L) {
								//Generation d'un message d'erreur s'il existe plusieurs obligations
								addError(CmMessageRepository90000.MSG_15());
							}
						}
					}
				}
			}
		}
	}
	

	/**
	 * Permet de recuperer les comptes d'un employeur a partir 
	 * de son identifiant system
	 * @param idEmployeur identifiant system de l'employeur
	 * @return List<Account> la liste des comptes de l'employeur
	 */
	public List<Account> getAccountsByIdPerson(String idEmployeur) {
		List<Account> listeAccounts = new ArrayList<Account>();
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetPersonAccounts");
		bsInstance.set("personId", idEmployeur);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		//List des comptes ratachés à l'employeur
		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			Account_Id accountId=new Account_Id(nextElt.getNumber("accountId").toString());
			listeAccounts.add(accountId.getEntity());
		}
		return listeAccounts;
	}
	
	/**
	 * Permet de retrouver une person à partir de ces identifiant: 
	 * @param IdType type d'identifiant exemple: NINEA
	 * @param idNumber numéro d'identifant
	 * @return Person la person qui posséde le numéro d'identifiant
	 */
	private Person getPersonByIdNumber(String IdType, String idNumber) {
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		return perSearch.searchPerson(idType.getEntity(), idNumber);
	}
	/**
	 * 
	 * @param account
	 * @param obligationType
	 * @param filingPeriod
	 * @param taxRole
	 * @return
	 */
	private Query<ServiceAgreement> retrieveObligations(Account account, ServiceAgreementType obligationType,
			FilingPeriod filingPeriod, TaxRole taxRole) {
		Query query = null;
		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append("FROM ServiceAgreement sa ");
		hqlQuery.append("WHERE sa.account = :account ");
		hqlQuery.append("AND sa.serviceAgreementType = :saType ");
		hqlQuery.append("AND sa.filingPeriodId = :filingPeriodId ");
		hqlQuery.append("AND sa.status <> :status ");
		if (this.notNull(taxRole)) {
			hqlQuery.append("AND sa.taxRoleId = :taxRoleId ");
			query = this.createQuery(hqlQuery.toString());
			query.bindId("taxRoleId", taxRole.getId());
		} else {
			query = this.createQuery(hqlQuery.toString());
		}
		
		query.bindEntity("account", account);
		query.bindEntity("saType", obligationType);
		query.bindId("filingPeriodId", filingPeriod.getId());
		query.bindLookup("status", ServiceAgreementStatusLookup.constants.CANCELED);
		return query;
	}
	
	private void retrieveFormRuleDetails() {
		FormRule_Id formRuleId = this.inputData.getFormRuleId();
		MaintenanceObjectInfo moInfo = MaintenanceObjectInfoCache
				.getMaintenanceObjectInfo(formRuleId.metaInfo().getTableId());
		BusinessObjectInfo boInfo = moInfo.determineBusinessObjectInfo(formRuleId);
		this.ruleBoInstance = BusinessObjectInstance.create(boInfo.getBusinessObject());
		this.ruleBoInstance.set("formRuleGroup", formRuleId.getFormRuleGroup());
		this.ruleBoInstance.set("formRule", formRuleId.getFormRule());
		this.ruleBoInstance = BusinessObjectDispatcher.read(this.ruleBoInstance);
	}
	
	
	private TaxRole getTagRoleByAccountId(String accountId){
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-RetTaxRolesOfAccountList");
		bsInstance.set("accountId", accountId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		// System.out.println("Adjustement ID: " +bsInstance.getString(name));
		TaxRole resultat = null;
		COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		System.out.println("liste: " + list);
		// If list IS NOT empty
		if (!list.isEmpty()) {
			COTSInstanceListNode fistRow = list.iterator().next();
			if (fistRow != null) {
				TaxRole_Id taxRoleId=new TaxRole_Id(fistRow.getString("taxRoleId"));
				resultat = taxRoleId.getEntity();
			}
		}
		return resultat;
	}
	
	private String getFactorVal(String factor, String dateDebutCotisation, String dateFinCotisation) {
		PreparedStatement preparedStatement = createPreparedStatement(
				"SELECT FACTOR_VAL FROM C1_FACTOR_VALUE where FACTOR_CD=:factor and TO_CHAR(EFFDT,'DD/MM/YYYY') <=:effectiveDate order by EFFDT DESC");
		preparedStatement.bindString("factor", factor, null);
		preparedStatement.bindString("effectiveDate", dateFinCotisation, null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		return sqlResultRow.getString("FACTOR_VAL");
	}
	
	/**
	 * Permet de recuperer une instance du BO du Form Rule
	 * @return BusinessObjectInstance du BO du Form Rule
	 */
	private BusinessObjectInstance getRuleDetails() {
		MaintenanceObjectInfo moInfo = MaintenanceObjectInfoCache
				.getMaintenanceObjectInfo(this.inputData.getFormRuleId().metaInfo().getTableId());
		BusinessObjectInfo boInfo = moInfo.determineBusinessObjectInfo(this.inputData.getFormRuleId());
		BusinessObjectInstance boInstance = BusinessObjectInstance.create(boInfo.getBusinessObject());
		boInstance.set("formRuleGroup", this.inputData.getFormRuleId().getFormRuleGroup().getId().getIdValue());
		boInstance.set("formRule", this.inputData.getFormRuleId().getFormRule());
		boInstance = BusinessObjectDispatcher.read(boInstance);
		return boInstance;
	}

}
