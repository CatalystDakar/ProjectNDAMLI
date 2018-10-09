package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.cm.domain.customMessages.CmMessageRepository90000;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;

/**
 * @author Papa
 *
 * @AlgorithmComponent ()
 */
public class CmCreateObligation_Impl extends CmCreateObligation_Gen implements FormRuleBORuleProcessingAlgorithmSpot {

	private ApplyFormRuleAlgorithmInputData inputData;
	private ApplyFormRuleAlgorithmInputOutputData inputOutputData;
	Logger logger = LoggerFactory.getLogger(CmCreateObligation_Impl.class);
	private static String TYPE_DECLARATION_MENSUEL = "MENSUEL";

	@Override
	public void invoke() {

		// TODO Auto-generated method stub
		BusinessObjectInstance formBoInstance = (BusinessObjectInstance) inputOutputData.getFormBusinessObject();
		
		
		String adjustedFromFormID = formBoInstance.getString("adjustedFromForm");
		//Condition pour vérifier un ajustment de formulaire
	    if(isNull(adjustedFromFormID)) {

		String idFormulaire = formBoInstance.getString("taxFormId");

		logger.info("idFormulaire: " + idFormulaire);

		logger.info("formBoInstanceXXXXX: " + formBoInstance.getSchemaName());
		// Form Rule
		FormRule formRule = inputData.getFormRuleId().getEntity();

		BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject());
		logger.info("formRuleBoInstance: " + formRuleBoInstance.getSchemaName());
		logger.info("formRuleBoInstance: " + formRuleBoInstance.getDocument().asXML());
		formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
		formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
		formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
		formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
		formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);

		Date dateDebutCotisation = (Date) formBoInstance
				.getFieldAndMDForPath("informationEmployeur/dateDebutCotisation/asCurrent").getValue();
		Date dateFinCotisation = (Date) formBoInstance
				.getFieldAndMDForPath("informationEmployeur/dateFinCotisation/asCurrent").getValue();
		logger.info("After form rule BO");

		// Form Rule Details Group
		String factorNbreSal = formRuleBoInstance.getString("nbrSal");

		String typeIdentifiant = (String) formBoInstance
				.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getValue();
		String idNumber = (String) formBoInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent")
				.getValue();
		
		String typeDeclaration = (String) formBoInstance.getFieldAndMDForPath("informationEmployeur/typeDeclaration/asCurrent")
				.getValue();

		String idEmployeur = getPersonByNinea(typeIdentifiant, idNumber).getId().getIdValue();

		int toTalSalaries = 0;
		BigDecimal totalSalaries = (BigDecimal) formBoInstance.getFieldAndMDForPath("synthese/totalSalaries/asCurrent")
				.getValue();// getNombreEmployes(idEmployeur);
		if (totalSalaries != null) {
			toTalSalaries = totalSalaries.intValue();
			logger.info("nombreSalaries" + toTalSalaries);
		}

		logger.info("dateDebutCotisation: " + dateDebutCotisation);
		logger.info("dateFinCotisation: " + dateFinCotisation);
		String nombreSal = getFactorVal(factorNbreSal, dateDebutCotisation.toString(), dateFinCotisation.toString());
		BigDecimal nombreSalToBD = new BigDecimal(nombreSal);

		logger.info("nombreSal " + nombreSal);

		logger.info("nombreSalToBD " + nombreSalToBD);

		logger.info("nombreSalInt " + nombreSalToBD.intValue());
		BigDecimal nbko = new BigDecimal(nombreSalToBD.intValue());
		// int nombreSalNumerique = Integer.parseInt(nombreSal);

		Money montantPF = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantPF/asCurrent").getValue();
		Money montantATMP = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantATMP/asCurrent").getValue();
		Money montantRRG = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantRRG/asCurrent").getValue();
		Money montantRCC = (Money) formBoInstance.getFieldAndMDForPath("synthese/montantRCC/asCurrent").getValue();
		double montantTotalDouble = montantRCC.getAmount().doubleValue() + montantRRG.getAmount().doubleValue();
		BigDecimal montantTotalBD = new BigDecimal(montantTotalDouble);
		Money montantTotalIPRES = new Money(montantTotalBD);
		String compte = getAccountsByIdPerson(idEmployeur);
		List<BigDecimal> listesequences = getSequences();
		List<String> taxRoles = getTaxRolesByAccountId(compte);
		BusinessObjectInstance obligationInstance = BusinessObjectInstance.create("C1-FilingPeriodObligation");
		obligationInstance.set("accountId", compte);
		for (String taxRoleId : taxRoles) {
			String taxType = getTaxTypeByTaxeRoleId(taxRoleId);
			obligationInstance.set("taxRole", taxRoleId);
			if (!verifierDateFinMonThLy(dateFinCotisation)) {
				addError(CmMessageRepository90000.MSG_14());
			} else {
				if (TYPE_DECLARATION_MENSUEL.equalsIgnoreCase(typeDeclaration)) {
					updateOrInsertCalendrier(taxRoleId, dateDebutCotisation, "SU-MONTHLY");
					obligationInstance.set("filingCalendar", "SU-MONTHLY");
				} else if (!verifierDateFinCotisation(dateFinCotisation)) {
					addError(CmMessageRepository90000.MSG_13(idNumber, nbko.toString()));
				} else {
					updateOrInsertCalendrier(taxRoleId, dateDebutCotisation, "SU-QUARTER");
					obligationInstance.set("filingCalendar", "SU-QUARTER");
				}
			}
			obligationInstance.set("filingCalendarEndDate", dateFinCotisation);
			obligationInstance.set("obligationStatus", ServiceAgreementStatusLookup.constants.PENDING_START);
			obligationInstance.set("startDate", dateDebutCotisation);
			obligationInstance.set("endDate", dateFinCotisation);

			for (BigDecimal sequence : listesequences) {
				List<String> listeOptionValuesBySeq = getOptionValues(sequence.intValue());
				Map<String, String> mapOptionValues = new HashMap<String, String>();
				mapOptionValues.put("OBLIGATION", listeOptionValuesBySeq.get(0));
				mapOptionValues.put("AJUSTEMENT", listeOptionValuesBySeq.get(1));
				mapOptionValues.put("TAXE", listeOptionValuesBySeq.get(2));
				String optioValue = mapOptionValues.get("TAXE");
				String division = getDivisionByObligationType(mapOptionValues.get("OBLIGATION"));
				if (taxType.equals(optioValue)) {
					obligationInstance.set("taxRole", taxRoleId);
					obligationInstance.set("division", division);
					obligationInstance.set("obligationType", mapOptionValues.get("OBLIGATION"));
					obligationInstance = BusinessObjectDispatcher.add(obligationInstance);
					String obligationId = obligationInstance.getString("obligationId");

					// creation ID_DNS pour obligation
					ajouterDNSObligation(obligationId, idFormulaire, idFormulaire, dateDebutCotisation);
					logger.info("obligationId: " + obligationId);
					String obligationType = obligationInstance.getString("obligationType");

					if (obligationType.equals("O-EPF")) {
						if (dateFinCotisation.getYear() != getSystemDateTime().getDate().getYear()) {
							String idAdjustment = createAjustementBS("CPFHE", obligationId, montantPF.getAmount(),
									dateDebutCotisation); // CPFHE
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentPF: " + idAdjustment);
						} else {
							String idAdjustment = createAjustementBS(mapOptionValues.get("AJUSTEMENT"), obligationId,
									montantPF.getAmount(), dateDebutCotisation); // CPF
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentPF: " + idAdjustment);
						}

					} else if (obligationType.equals("O-EATMP")) {
						if (dateFinCotisation.getYear() != getSystemDateTime().getDate().getYear()) {
							String idAdjustment = createAjustementBS("CATMPHE", obligationId, montantATMP.getAmount(),
									dateDebutCotisation); // CATMPHE
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentPF: " + idAdjustment);
						} else {
							String idAdjustment = createAjustementBS(mapOptionValues.get("AJUSTEMENT"), obligationId,
									montantATMP.getAmount(), dateDebutCotisation);
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentATMP: " + idAdjustment);  
						}

					} else if (obligationType.equals("O-ER")) {
						if (dateFinCotisation.getYear() != getSystemDateTime().getDate().getYear()) {
							String idAdjustment = createAjustementBS("CRHE", obligationId,
									montantTotalIPRES.getAmount(), dateDebutCotisation); // CRHE
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentPF: " + idAdjustment);
						} else {
							String idAdjustment = createAjustementBS(mapOptionValues.get("AJUSTEMENT"), obligationId,
									montantTotalIPRES.getAmount(), dateDebutCotisation);
							ajouterDNSAdj(idAdjustment, idFormulaire, idFormulaire);
							String idGroupeFT=getFtIdByAdjId(idAdjustment);
							updateGroupFtId(idGroupeFT); 
							logger.info("idAdjustmentVIEILLESSE: " + idAdjustment);
						}

					}
				}
				if (taxType.equals(optioValue)) {
					break;
				}
			}
		  }
	   }
	}

	private boolean verifierDateFinCotisation(Date date) {
		boolean ok = false;
		if ((date.getDay() == 31 && date.getMonth() == 3) || (date.getDay() == 30 && date.getMonth() == 6)
				|| (date.getDay() == 30 && date.getMonth() == 9) || (date.getDay() == 31 && date.getMonth() == 12)) {
			ok = true;
		}
		return ok;

	}

	private boolean verifierDateFinMonThLy(Date date) {
		boolean ok = false;
		if (((date.getDay() == 30)
				&& (date.getMonth() == 4 || date.getMonth() == 6 || date.getMonth() == 9 || date.getMonth() == 11))
				|| ((date.getDay() == 31)
						&& (date.getMonth() == 1 || date.getMonth() == 3 || date.getMonth() == 5 || date.getMonth() == 7
								|| date.getMonth() == 8 || date.getMonth() == 10 || date.getMonth() == 12))
				|| ((date.getDay() == 28) && (date.getMonth() == 2))
				|| ((date.getDay() == 29) && (date.getMonth() == 2))) {
			ok = true;
		}
		return ok;

	}

	private void updateCalendrierTaxRole(String clendrier, String idTaxRole, Date dateEffet) {
		String query = "UPDATE CI_TAX_ROLE_CAL SET FILING_CAL_CD=:clendrierSoft, EFFDT=:dateEffetSoft WHERE TAX_ROLE_ID=:idTaxRoleSoft";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("clendrierSoft", clendrier, null);
		preparedStatement.bindDate("dateEffetSoft", dateEffet);
		preparedStatement.bindString("idTaxRoleSoft", idTaxRole, null);
		preparedStatement.executeUpdate();
		System.out.println("Mise a jour reussie");
	}

	private void ajoutCalendrierTaxRole(String idTaxRole, Date effetDate, String filingCal) {
		String query = "INSERT INTO CI_TAX_ROLE_CAL(TAX_ROLE_ID, EFFDT, FILING_CAL_CD) VALUES(:idTaxRoleSoft, :effetDateSoft, :filingCalSoft)";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("idTaxRoleSoft", idTaxRole, null);
		preparedStatement.bindDate("effetDateSoft", effetDate);
		preparedStatement.bindString("filingCalSoft", filingCal, null);
		preparedStatement.executeUpdate();
	}

	private String getNombreEmployes(String idEmployeur) {
		// TODO Auto-generated method stub
		String resultat = null;
		String query = "SELECT ADHOC_CHAR_VAL  FROM CI_PER_CHAR  WHERE CHAR_TYPE_CD='CM-GENNO' and PER_ID=:perId";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("perId", idEmployeur, null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();

		if (sqlResultRow != null) {
			resultat = sqlResultRow.getString("ADHOC_CHAR_VAL");
			System.out.println("RESULTAT SQL= " + resultat);
		}
		return resultat;
	}

	private String getDivisionByObligationType(String obligationType) {
		// TODO Auto-generated method stub
		String resultat = null;
		String query = "SELECT CIS_DIVISION FROM CI_SA_TYPE WHERE SA_TYPE_CD='" + obligationType + "'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		// preparedStatement.bindString("obligationType", obligationTypes,
		// null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();

		if (sqlResultRow != null) {
			resultat = sqlResultRow.getString("CIS_DIVISION");
			System.out.println("RESULTAT SQL= " + resultat);
		}
		return resultat;
	}

	public void updateOrInsertCalendrier(String idTaxRole, Date effetDate, String filingCal) {
		String query = "SELECT * FROM CI_TAX_ROLE_CAL WHERE TAX_ROLE_ID =:idTaxRoleSoft AND EFFDT =:effetDateSoft";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("idTaxRoleSoft", idTaxRole, null);
		preparedStatement.bindDate("effetDateSoft", effetDate);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		System.out.println(sqlResultRow);
		if (sqlResultRow != null) {
			if (!sqlResultRow.getString("FILING_CAL_CD").trim().equals(filingCal)) {
				updateCalendrierTaxRole(filingCal, idTaxRole, effetDate);
			}
		} else {
			ajoutCalendrierTaxRole(idTaxRole, effetDate, filingCal);
		}

		// if(sqlResultRow.getInteger("nb") > 0)
		// Update
		// else
		// Insert
	}

	public String getAccountsByIdPerson(String personId) {
		String result = null;
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetPersonAccounts");

		bsInstance.set("personId", personId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceList list = bsInstance.getList("results");
		if (!list.isEmpty()) {
			COTSInstanceListNode fistRow = list.iterator().next();
			if (fistRow != null) {
				result=fistRow.getFieldAndMDForPath("accountId").getXMLValue(); 
			}

		}
		return result;
	}

	public List<String> getTaxRolesByAccountId(String accountId) {
		List<String> listeTaxRoles = new ArrayList<String>();
		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-RetTaxRolesOfAccountList");

		bsInstance.set("accountId", accountId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		// System.out.println("Adjustement ID: " +bsInstance.getString(name));
		// String resultat = null;
		// COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		// System.out.println("liste: " + list);
		// If list IS NOT empty
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			System.out.println("TaxRoleId: " + nextElt.getString("taxRoleId"));
			System.out.println("TaxRoleInformation: " + nextElt.getString("taxRoleInformation"));
			listeTaxRoles.add(nextElt.getString("taxRoleId"));

		}
		return listeTaxRoles;

	}

	public String getTaxTypeByTaxeRoleId(String taxRoleId) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-RETTRTT");

		bsInstance.set("taxRoleId", taxRoleId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		// System.out.println("Adjustement ID: " +bsInstance.getString(name));
		String resultat = null;
		COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		System.out.println("liste: " + list);
		// If list IS NOT empty
		if (!list.isEmpty()) {
			COTSInstanceListNode fistRow = list.iterator().next();
			if (fistRow != null) {
				System.out.println("TaxType: " + fistRow.getString("taxType"));
				resultat=fistRow.getString("taxType");
			}

		}
		return resultat;
	}

	public List<String> getFilingCalendarByTaxetype(String taxType) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-RetTaxTypeValCalendarList");

		bsInstance.set("taxType", taxType);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		// System.out.println("Adjustement ID: " +bsInstance.getString(name));
		List<String> listFilingCalendar = new ArrayList<String>();
		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		// COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		// System.out.println("liste: " +list);
		// If list IS NOT empty
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			if (nextElt != null) {
				System.out.println("FilingCalendar: " + nextElt.getString("filingCalendar"));
				listFilingCalendar.add(nextElt.getString("filingCalendar"));
				System.out.println("Description: " + nextElt.getString("description"));
			}

		}
		return listFilingCalendar;
	}

	public String getDivisionByTaxRoleId(String taxRoleId) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-TXRLDIVS");

		bsInstance.set("taxRoleId", taxRoleId);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		COTSInstanceList list = bsInstance.getList("results");
		String resultat = null;
		// COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		// System.out.println("liste: " +list);
		// If list IS NOT empty
		if (!list.isEmpty()) {
			COTSInstanceListNode nextElt = list.iterator().next();
			if (nextElt != null) {
				System.out.println("Division: " + nextElt.getString("division"));
				System.out.println("Description: " + nextElt.getString("description"));
				resultat = nextElt.getString("division");
			}

		}
		return resultat;
	}

	public List<String> getObligationTypeByTaxetype(String taxType) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-GetObligationTypeForTaxType");
		// ObligationTypes c=new
		bsInstance.set("taxType", taxType);
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);
		// System.out.println("Adjustement ID: " +bsInstance.getString(name));
		List<String> listeObligationType = new ArrayList<String>();
		Iterator<COTSInstanceListNode> iterator = bsInstance.getList("results").iterator();
		// COTSInstanceList list = bsInstance.getList("results");
		// COTSInstanceList list = bsInstance.getList("results");
		// System.out.println("liste: " +list);
		// If list IS NOT empty
		while (iterator.hasNext()) {
			COTSInstanceListNode nextElt = iterator.next();
			if (nextElt != null) {
				System.out.println("ObligationType: " + nextElt.getString("obligationType"));
				listeObligationType.add(nextElt.getString("obligationType"));
				System.out.println("ObligationTypeDescription: " + nextElt.getString("obligationTypeDescription"));
			}

		}
		return listeObligationType;
	}

	private Person getPersonByNinea(String IdType, String idNumber) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		// log.info("*****ID Type: " + idType.getTrimmedValue());
		return perSearch.searchPerson(idType.getEntity(), idNumber);
	}

	private void ajouterDNSAdj(String adjId, String valFk1, String srchval) {
		String query = "INSERT INTO CI_ADJ_CHAR(ADJ_ID, CHAR_VAL_FK1, SRCH_CHAR_VAL, CHAR_TYPE_CD, SEQ_NUM) VALUES(:adjIdSoft, :valFk1Soft, :srchvalSoft, 'ID_DNS' ,1)";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("adjIdSoft", adjId, null);
		preparedStatement.bindString("valFk1Soft", valFk1, null);
		preparedStatement.bindString("srchvalSoft", srchval, null);
		preparedStatement.executeUpdate();

	}

	private void ajouterDNSObligation(String obligationId, String valFk1, String srchval, Date dateEff) {
		String query = "INSERT INTO CI_SA_CHAR(SA_ID, EFFDT, CHAR_VAL_FK1, SRCH_CHAR_VAL, CHAR_TYPE_CD) VALUES(:obligationIdSoft, :effdtSoft, :valFk1Soft, :srchvalSoft, 'ID_DNS')";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("obligationIdSoft", obligationId, null);
		preparedStatement.bindDate("effdtSoft", dateEff);
		preparedStatement.bindString("valFk1Soft", valFk1, null);
		preparedStatement.bindString("srchvalSoft", srchval, null);
		preparedStatement.executeUpdate();
	}

	public void createAjustement(String adjustType, String obligationId, Money adjustmentAmount) {

		// Business Service Instance
		BusinessServiceInstance bsInstance = BusinessServiceInstance.create("C1-CancelCreateAdjustments");
		// Money money=new Money("8750");

		// Populate BS parameters if available
		if (null != adjustType && null != obligationId && null != adjustmentAmount) {
			COTSInstanceNode group = bsInstance.getGroupFromPath("input");
			COTSInstanceListNode firstRow = group.getList("newAdjustments").newChild();
			// COTSInstanceListNode firstRow = list.iterator().next();
			firstRow.set("adjustmentType", adjustType);// ASMT-WO
			firstRow.set("obligationId", obligationId); // createObligation("9045036816",
														// "DOR","E-TPERCU")
			firstRow.set("adjustmentAmount", adjustmentAmount);
			System.out.println(getSystemDateTime().getDate());
			firstRow.set("adjustmentDate", getSystemDateTime().getDate());
		}

		// Execute BS and return the Ninea if exists
		bsInstance = BusinessServiceDispatcher.execute(bsInstance);

	}

	private String createAjustementBS(String adjustType, String obligationId, BigDecimal adjustmentAmount,
			Date effectiveDate) {

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
	private String getFtIdByAdjId(String adjId){
		String resultat=null;
		String query = "SELECT * FROM CI_FT WHERE SIBLING_ID=:adjIdSoft  AND FT_TYPE_FLG = 'AD'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("adjIdSoft", adjId, null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		if (sqlResultRow != null) {
			resultat=sqlResultRow.getString("FT_ID");
		}
	
		return resultat; 
		
	}
	
	private void updateGroupFtId(String ftId){
		String query = "UPDATE CI_FT SET GRP_FT_ID=:ftIdSoft1 WHERE FT_ID=:ftIdSoft2";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		preparedStatement.bindString("ftIdSoft1", ftId, null);
		preparedStatement.bindString("ftIdSoft2", ftId, null);
		preparedStatement.executeUpdate();
		System.out.println("MISA JOUR REUSSI");
		
	}
	public List<BigDecimal> getSequences() {
		List<BigDecimal> listeSequences = new ArrayList<BigDecimal>();
		String query = "SELECT DISTINCT(SEQ_NUM) FROM CI_WFM_OPT WHERE WFM_NAME='CMCO'";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		QueryIterator<SQLResultRow> iter1 = preparedStatement.iterate();
		while (iter1.hasNext()) {
			SQLResultRow result = (SQLResultRow) iter1.next();
			BigDecimal sequence = result.getBigDecimal("SEQ_NUM");
			System.out.println("SEQ " + sequence);
			listeSequences.add(sequence);

		}
		return listeSequences;
	}

	public List<String> getOptionValues(int seq) {
		List<String> listeAccountType = new ArrayList<String>();
		String query = "SELECT WFM_OPT_VAL FROM CI_WFM_OPT WHERE WFM_NAME='CMCO' AND SEQ_NUM=:sequence";
		PreparedStatement preparedStatement = createPreparedStatement(query);
		BigDecimal sequence = new BigDecimal(seq);
		preparedStatement.bindBigDecimal("sequence", sequence);
		QueryIterator<SQLResultRow> iter1 = preparedStatement.iterate();
		while (iter1.hasNext()) {
			SQLResultRow result = (SQLResultRow) iter1.next();
			String accountType = result.getString("WFM_OPT_VAL");
			listeAccountType.add(accountType);
		}
		return listeAccountType;
	}

	private String getFactorVal(String factor, String dateDebutCotisation, String dateFinCotisation) {
		PreparedStatement preparedStatement = createPreparedStatement(
				"SELECT FACTOR_VAL FROM C1_FACTOR_VALUE where FACTOR_CD=:factor and TO_CHAR(EFFDT,'DD/MM/YYYY') <=:effectiveDate order by EFFDT DESC");
		preparedStatement.bindString("factor", factor, null);
		preparedStatement.bindString("effectiveDate", dateFinCotisation, null);
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		return sqlResultRow.getString("FACTOR_VAL");
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
