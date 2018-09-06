package com.splwg.cm.domain.admin.formRule;

import java.util.Iterator;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.cm.domain.common.businessComponent.CmPersonSearchComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
//import com.splwg.tools.artifactgen.metadata.Lookup;
import com.splwg.base.api.datatypes.Lookup;

/**
 * @author Papa
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = etatDefinitif, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = etatIntermediaire, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = typeProcessus, required = true, type = string)})
 */
public class CmDeclarationSalaries_Impl extends CmDeclarationSalaries_Gen
		implements FormRuleBORuleProcessingAlgorithmSpot {
	
	Logger logger = LoggerFactory.getLogger(CmDeclarationSalaries_Impl.class);

	private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
	private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;

	@SuppressWarnings("unchecked")
	@Override
	public void invoke() { 
		// TODO Auto-generated method stub
				BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData
						.getFormBusinessObject();
				String typeIdentifiant = (String) formBoInstance
						.getFieldAndMDForPath("informationEmployeur/typeIdentifiant/asCurrent").getValue();
				String idNumber = (String) formBoInstance.getFieldAndMDForPath("informationEmployeur/idNumber/asCurrent")
						.getValue();
				Date dateDebutCotisation = (Date) formBoInstance.getFieldAndMDForPath("informationEmployeur/dateDebutCotisation/asCurrent").getValue();
				Date dateFinCotisation = (Date) formBoInstance.getFieldAndMDForPath("informationEmployeur/dateFinCotisation/asCurrent").getValue();
//				dateDebutCotisation

				String idEmployeur = getPersonByIdNumber(typeIdentifiant, idNumber).getId().getIdValue();
				
				//Recuperation des champs
				COTSInstanceNode group = formBoInstance.getGroupFromPath("informationSalaries");
				Iterator<COTSInstanceListNode> iterator = group.getList("informationSalariesList").iterator();
				while (iterator.hasNext()) {
					COTSInstanceListNode nextElt = iterator.next();
					if (nextElt != null) {
						BusinessObjectInstance dnsBoInstance=null; 
						COTSFieldDataAndMD<String> prenom = nextElt.getFieldAndMDForPath("prenom/asCurrent");
						COTSFieldDataAndMD<String> nom = nextElt.getFieldAndMDForPath("nom/asCurrent");
						COTSFieldDataAndMD<Date> dateNaissance = nextElt.getFieldAndMDForPath("dateDeNaisssance/asCurrent");
						String typePiece = (String) nextElt.getFieldAndMDForPath("typePieceIdentite/asCurrent").getValue();
						String numeroPiece = nextElt.getFieldAndMDForPath("numeroPieceIdentite/asCurrent").getXMLValue();
						COTSFieldDataAndMD<Bool> regimCompCadre= nextElt.getFieldAndMDForPath("regimCompCadre/asCurrent");
						COTSFieldDataAndMD<Bool> regimeGeneral= nextElt.getFieldAndMDForPath("regimeGeneral/asCurrent");
						COTSFieldDataAndMD<Date> dateEffetRegime =  nextElt.getFieldAndMDForPath("dateEffetRegime/asCurrent");
						Money totSalAssIpresRge = (Money) nextElt.getFieldAndMDForPath("totSalAssIpresRge/asCurrent").getValue();
						Money totSalAssIpresRcce = (Money) nextElt.getFieldAndMDForPath("totSalAssIpresRcce/asCurrent").getValue();
						Money totSalAssCssAtmpe = (Money) nextElt.getFieldAndMDForPath("totSalAssCssAtmpe/asCurrent").getValue();
						Money totSalAssCssPfe = (Money) nextElt.getFieldAndMDForPath("totSalAssCssPfe/asCurrent").getValue();
						COTSFieldDataAndMD<Money> salaireBrut = nextElt.getFieldAndMDForPath("salaireBrut/asCurrent");
						String typeContrat =  nextElt.getFieldAndMDForPath("typeContrat/asCurrent").getXMLValue();
						COTSFieldDataAndMD<Date> dateEntree =  nextElt.getFieldAndMDForPath("dateEntree/asCurrent");
						COTSFieldDataAndMD<Date> dateSortie =  nextElt.getFieldAndMDForPath("dateSortie/asCurrent");
						String motifSortie =  nextElt.getFieldAndMDForPath("motifSortie/asCurrent").getXMLValue();
						COTSFieldDataAndMD<BigDecimal> nombreJours =nextElt.getFieldAndMDForPath("nombreJours/asCurrent");
						COTSFieldDataAndMD<BigDecimal> nombreHeures = nextElt.getFieldAndMDForPath("nombreHeures/asCurrent");
						String  tempsTravail = nextElt.getFieldAndMDForPath("tempsTravail/asCurrent").getXMLValue();
						COTSFieldDataAndMD<String> trancheTravail = nextElt.getFieldAndMDForPath("trancheTravail/asCurrent");
//						Money montantCssCpf = (Money) nextElt.getFieldAndMDForPath("montantCssCpf/asCurrent").getValue();
//						Money montantCssAtMp = (Money) nextElt.getFieldAndMDForPath("montantCssAtMp/asCurrent").getValue();
//						Money montantIpresCrrg = (Money) nextElt.getFieldAndMDForPath("montantIpresCrrg/asCurrent").getValue();
//						Money montantIpresCrcc = (Money) nextElt.getFieldAndMDForPath("montantIpresCrcc/asCurrent").getValue();
						
						Money totalSalCSS=totSalAssCssPfe.add(totSalAssCssAtmpe);
						Money totalSalIPRES=totSalAssIpresRge.add(totSalAssIpresRcce);
						
						Person person=getPersonByIdNumber(typePiece, numeroPiece);
						String employeCadreTostr = null;

						if (regimCompCadre != null) {
							employeCadreTostr = regimCompCadre.getValue().toString();
						} else if (regimeGeneral != null) {
							employeCadreTostr = regimeGeneral.getValue().toString();
						}
						dnsBoInstance=BusinessObjectInstance.create("CMDMT_HistoriqueBO");
						
						dnsBoInstance.set("cmTypeProcessus", getTypeProcessus());
						if(person==null){
							//envoyer une ToDo
							dnsBoInstance.set("cmEtatProcessus", getEtatIntermediaire());
						}
						else{
							String idTravailleur=person.getId().getIdValue();
							dnsBoInstance.set("cmIdTravailleur", idTravailleur);
							dnsBoInstance.set("cmEtatProcessus", getEtatDefinitif()); 
						}
						
						
						
						if(nom!=null){
							dnsBoInstance.set("cmNom", nom.getValue());	
						}
						if(prenom!=null){
							dnsBoInstance.set("cmPrenom", prenom.getValue());
						}
						if(dateEffetRegime!=null){
							dnsBoInstance.set("cmDateEffetRegime",dateEffetRegime.getValue());   
						}
						if(salaireBrut!=null){  
							dnsBoInstance.set("cmSalaireContractuel", salaireBrut.getValue());
						}
						
						if(typeContrat!=null){
							dnsBoInstance.set("cmNatureContrat", typeContrat);
						}  
						
						if(dateEntree!=null){
							dnsBoInstance.set("cmDateDebutContrat", dateEntree.getValue());
						}
						if(dateSortie!=null){
							dnsBoInstance.set("cmDateFinContrat", dateSortie.getValue());
						}
						if(nombreHeures!=null){
							dnsBoInstance.set("cmTempsPresenceHeure", nombreHeures.getValue());
						}
						if(nombreJours!=null){
							dnsBoInstance.set("cmTempsPresenceJour", nombreJours.getValue());
						}
						if(tempsTravail != null){
							dnsBoInstance.set("cmTpsDeTravail", tempsTravail);
						}
						if(motifSortie!=null){
							dnsBoInstance.set("cmMotif", motifSortie);
						}
						
						if(employeCadreTostr!=null){
							dnsBoInstance.set("cmEmployeCadre", employeCadreTostr);	
						}
						if(trancheTravail!=null){
							dnsBoInstance.set("cmTrancheTravail", trancheTravail.getValue());
						}
						
						dnsBoInstance.set("cmTotalSalCSS", totalSalCSS);
						dnsBoInstance.set("cmTotalSalIPRES", totalSalIPRES);
						dnsBoInstance.set("cmTypePiece", typePiece);
						dnsBoInstance.set("cmNumero", numeroPiece.toString());
						dnsBoInstance.set("cmDateDebutPeriodeCotisation", dateDebutCotisation);
						dnsBoInstance.set("cmDateFinPeriodeCotisation", dateFinCotisation);
						dnsBoInstance.set("cmIdEmployeur", idEmployeur);
						dnsBoInstance = BusinessObjectDispatcher.add(dnsBoInstance);
						
						

					}
				}
				

	}

	private Person getPersonByIdNumber(String IdType, String idNumber) {
		// log.info("*****Starting getpersonId");
		CmPersonSearchComponent perSearch = new CmPersonSearchComponent.Factory().newInstance();
		IdType_Id idType = new IdType_Id(IdType);
		// log.info("*****ID Type: " + idType.getTrimmedValue());
		return perSearch.searchPerson(idType.getEntity(), idNumber);
	}

	@Override
	public void setApplyFormRuleAlgorithmInputData(
			ApplyFormRuleAlgorithmInputData paramApplyFormRuleAlgorithmInputData) {
		applyFormRuleAlgorithmInputData = paramApplyFormRuleAlgorithmInputData;
	}

	@Override
	public void setApplyFormRuleAlgorithmInputOutputData(
			ApplyFormRuleAlgorithmInputOutputData paramApplyFormRuleAlgorithmInputOutputData) {
		applyFormRuleAlgorithmInputOutputData = paramApplyFormRuleAlgorithmInputOutputData;
	}

	@Override
	public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
		return applyFormRuleAlgorithmInputOutputData;
	}
}
