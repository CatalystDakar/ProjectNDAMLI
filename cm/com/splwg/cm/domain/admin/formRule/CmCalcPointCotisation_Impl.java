package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSFieldDataAndMD;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectExitStatusAlgorithmSpot;
import com.splwg.cm.domain.common.entities.DetailCotisation;
import com.splwg.cm.domain.common.entities.InformationCalcPoint;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formType.FormType;
import com.splwg.tax.domain.admin.formType.FormType_Id;

/**
 * @author Papa
 *
* @AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name =formType, type = string)})
 */
public class CmCalcPointCotisation_Impl extends CmCalcPointCotisation_Gen
		implements BusinessObjectExitStatusAlgorithmSpot {
	private BusinessObjectInstanceKey boKey;
	private BusinessObjectInstance boInstance;
	private final static Logger log = LoggerFactory.getLogger(CmCalcPointCotisation_Impl.class);
	@Override
	public void invoke() {
		// TODO Auto-generated method stub
		
		
		this.boInstance = BusinessObjectDispatcher.read(this.boKey, false);
		 COTSFieldDataAndMD<?> numeroPiece =this.boInstance.getFieldAndMDForPath("dossierPrestationExterieure/nin");
		 BusinessObjectInstance boInstanceObj = createFormBOInstance(this.getFormType(), "CALCPOINTS-" + getSystemDateTime().toString());
		 List<DetailCotisation> pointsCotistaionRGeneral=getAllElementsRG(numeroPiece.getValue().toString());
		 List<DetailCotisation> pointsCotistaionRCadre=getAllElementsRC(numeroPiece.getValue().toString());
		 int nbreTotalPointsRG=formCreatorRG(pointsCotistaionRGeneral, boInstanceObj);
		 int nbreTotalPointsRC=formCreatorRC(pointsCotistaionRCadre, boInstanceObj);
		 
		 COTSFieldDataAndMD<?> totalPointsRG =boInstanceObj.getFieldAndMDForPath("calculPointCarriere/totalPointsRG/asCurrent");
		 totalPointsRG.setXMLValue(new BigDecimal(nbreTotalPointsRG).toString()); 
		 COTSFieldDataAndMD<?> totalPointsRCC =boInstanceObj.getFieldAndMDForPath("calculPointCarriere/totalPointsRCC/asCurrent");
		 totalPointsRCC.setXMLValue(new BigDecimal(nbreTotalPointsRC).toString()); 
		 COTSFieldDataAndMD<?> totalPoints =boInstanceObj.getFieldAndMDForPath("calculPointCarriere/totalPoints/asCurrent");
		 totalPoints.setXMLValue(new BigDecimal(nbreTotalPointsRG+nbreTotalPointsRC).toString()); 
		 
		 boInstanceObj=BusinessObjectDispatcher.add(boInstanceObj);
		 COTSFieldDataAndMD<?> cots = this.boInstance.getFieldAndMDForPath("carriere/form");
		 cots.setXMLValue(boInstanceObj.getFieldAndMDForPath("taxFormId").getXMLValue());
		 log.info("taxFormId " +boInstanceObj.getFieldAndMDForPath("taxFormId").getXMLValue());  
		 BusinessObjectDispatcher.update(this.boInstance);
		 
		 
		 

	}
	
	private BusinessObjectInstance createFormBOInstance(String formTypeString, String documentLocator) {

		FormType formType = new FormType_Id(formTypeString).getEntity();
		String formTypeBo = formType.getRelatedTransactionBOId().getTrimmedValue();

		log.info("#### Creating BO for " + formType);

		BusinessObjectInstance boInstance = BusinessObjectInstance.create(formTypeBo);

		log.info("#### Form Type BO MD Schema: " + boInstance.getSchemaMD());

		boInstance.set("bo", formTypeBo);
		boInstance.set("formType", formType.getId().getTrimmedValue());
		boInstance.set("receiveDate", getSystemDateTime().getDate());
		boInstance.set("documentLocator", documentLocator);

		return boInstance;

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
		this.boKey=arg0; 

	}
	
	private List<DetailCotisation> getAllElementsRG(String numeroPiece) {
		List<DetailCotisation> listeElements = new ArrayList<DetailCotisation>();
		DetailCotisation detailCotisation = null;
		String query = "select ENTITY_NAME Nom_Employeur, Tab_Dates.ANNEE ANNEE, Tab_Dates.DEBUT DEBUT,"
				+ " Tab_Dates.FIN FIN, SUM(CAST (Tab_Salaire.Salaire_Periode as NUMBER(20))) TOTAL_SALAIRE"
				+ " from (select distinct id_employeur Employeur, substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2)"
				+ " Annee, min (date_debut_periode_cotisation) Debut, max(date_fin_periode_cotisation) Fin"
				+ " from cm_dmt_historique where NUMERO_PIECE = '"+numeroPiece+"' group by id_employeur,"
				+ " substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2) order by Annee) Tab_Dates,"
				+ "(select distinct id_employeur Employeur, substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2)"
				+ " Annee, date_debut_periode_cotisation Debut, date_fin_periode_cotisation Fin,"
				+ " salaire_contractuel * (substr(TO_CHAR(date_fin_periode_cotisation,'DD/MM/YYYY'),4,2)-substr(TO_CHAR(date_debut_periode_cotisation, 'DD/MM/YYYY'),4,2)+1) Salaire_Periode"
				+ " from cm_dmt_historique where NUMERO_PIECE = '"+numeroPiece+"' order by Annee) Tab_Salaire, ci_per_name"
				+ " where Tab_Dates.Annee = Tab_Salaire.Annee and Tab_Dates.Employeur = Tab_Salaire.Employeur"
				+ " and per_id = Tab_Salaire.Employeur group by ENTITY_NAME, Tab_Dates.Annee, Tab_Dates.Debut, Tab_Dates.Fin order by Tab_Dates.Debut";
				PreparedStatement preparedStatementt = createPreparedStatement(query, "SELECT");
		QueryIterator<SQLResultRow> resultIterator = null;
		try {
			resultIterator = preparedStatementt.iterate();
			while (resultIterator.hasNext()) {
				SQLResultRow result = (SQLResultRow) resultIterator.next();
				detailCotisation = new DetailCotisation();
				detailCotisation.setNomEmployeur(result.getString("NOM_EMPLOYEUR"));
				detailCotisation.setAnnee(result.getString("ANNEE"));
				detailCotisation.setDateDebut(result.getDate("DEBUT"));
				detailCotisation.setDateFin(result.getDate("FIN"));
				detailCotisation.setToTalSalaire(result.getBigDecimal("TOTAL_SALAIRE")); 
				listeElements.add(detailCotisation);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		
		return listeElements;  
	}
	
	private List<DetailCotisation> getAllElementsRC(String numeroPiece) {
		List<DetailCotisation> listeElements = new ArrayList<DetailCotisation>();
		DetailCotisation detailCotisation = null;
		String query = "select ENTITY_NAME Nom_Employeur, Tab_Dates.ANNEE ANNEE, Tab_Dates.DEBUT DEBUT,"
				+ " Tab_Dates.FIN FIN, SUM(CAST (Tab_Salaire.Salaire_Periode as NUMBER(20))) TOTAL_SALAIRE"
				+ " from (select distinct id_employeur Employeur, substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2)"
				+ " Annee, min (date_debut_periode_cotisation) Debut, max(date_fin_periode_cotisation) Fin"
				+ " from cm_dmt_historique where NUMERO_PIECE = '"+numeroPiece+"' AND EMPLOYE_CADRE = 'true' group by id_employeur,"
				+ " substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2) order by Annee) Tab_Dates,"
				+ "(select distinct id_employeur Employeur, substr(TO_CHAR(date_debut_periode_cotisation,'DD/MM/YYYY'),-2)"
				+ " Annee, date_debut_periode_cotisation Debut, date_fin_periode_cotisation Fin,"
				+ " salaire_contractuel * (substr(TO_CHAR(date_fin_periode_cotisation,'DD/MM/YYYY'),4,2)-substr(TO_CHAR(date_debut_periode_cotisation, 'DD/MM/YYYY'),4,2)+1) Salaire_Periode"
				+ " from cm_dmt_historique where NUMERO_PIECE = '"+numeroPiece+"' AND EMPLOYE_CADRE = 'true' order by Annee) Tab_Salaire, ci_per_name"
				+ " where Tab_Dates.Annee = Tab_Salaire.Annee and Tab_Dates.Employeur = Tab_Salaire.Employeur"
				+ " and per_id = Tab_Salaire.Employeur group by ENTITY_NAME, Tab_Dates.Annee, Tab_Dates.Debut, Tab_Dates.Fin order by Tab_Dates.Debut";
				PreparedStatement preparedStatementt = createPreparedStatement(query, "SELECT");
		QueryIterator<SQLResultRow> resultIterator = null;
		try {
			resultIterator = preparedStatementt.iterate();
			while (resultIterator.hasNext()) {
				SQLResultRow result = (SQLResultRow) resultIterator.next();
				detailCotisation = new DetailCotisation();
				detailCotisation.setNomEmployeur(result.getString("NOM_EMPLOYEUR"));
				detailCotisation.setAnnee(result.getString("ANNEE"));
				detailCotisation.setDateDebut(result.getDate("DEBUT"));
				detailCotisation.setDateFin(result.getDate("FIN"));
				detailCotisation.setToTalSalaire(result.getBigDecimal("TOTAL_SALAIRE")); 
				listeElements.add(detailCotisation);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		
		return listeElements;  
	}
	
	private InformationCalcPoint getDataPoints(int annee, int regime) {
		InformationCalcPoint info = null;
		String query = "SELECT * FROM cm_rpcalcdata WHERE periode="+annee+" and regime_scheme="+regime+""; 
		PreparedStatement preparedStatement = createPreparedStatement(query, "SELECT");
		SQLResultRow sqlResultRow = preparedStatement.firstRow();
		try {
				info = new InformationCalcPoint();
				info.setPlafondSalAnnuel(sqlResultRow.getBigDecimal("PLAFOND_SAL_ANN"));
				info.setSalaireRef(sqlResultRow.getBigDecimal("SALAIRE_DE_REFERENCE"));
				info.setTauxContractuel(sqlResultRow.getBigDecimal("TAUX_CON_TRACTUEL"));
			}
		 catch(Exception e) {
			e.printStackTrace();
		} 
		
		return info;  
	}
	
	private int min(int a, int b){
		if(a<b)
			return a;
		else
			return b;
	}
	
	private int formCreatorRG(List<DetailCotisation> elements,BusinessObjectInstance boInstance) {

		//BusinessObjectInstance boInstance = BusinessObjectInstance.create(this.getFormType());

		COTSInstanceNode sectionRegimeGeneral = boInstance.getGroup("carriereRG");

		COTSFieldDataAndMD<BigDecimal> maxPk = sectionRegimeGeneral.getFieldAndMDForPath("maxPk");
		maxPk.setValue(new BigDecimal(elements.size()));
		int i=1;
		int nbreTotalPointsRG=0;
		
		for(DetailCotisation element: elements){
			
			InformationCalcPoint info=getDataPoints(element.getDateDebut().getYear(), 1);
			int nbrePointRG=(int) (min(element.getToTalSalaire().intValue(), info.getPlafondSalAnnuel().intValue())*info.getTauxContractuel().doubleValue()/info.getSalaireRef().intValue());
			
			COTSInstanceListNode list = sectionRegimeGeneral.getList("carriereRGList").newChild();
			
			COTSFieldDataAndMD<BigDecimal> pk = list.getFieldAndMDForPath("pk");
			pk.setValue(new BigDecimal(i)); 
			
			COTSFieldDataAndMD<?> etablissement = list.getFieldAndMDForPath("etablissementRG/asCurrent");
			etablissement.setXMLValue(element.getNomEmployeur());
			
			COTSFieldDataAndMD<?> DateDebut = list.getFieldAndMDForPath("anneeDuRG/asCurrent");
			DateDebut.setXMLValue(element.getDateDebut().toString());
			
			COTSFieldDataAndMD<?> dateFin = list.getFieldAndMDForPath("auRG/asCurrent");
			dateFin.setXMLValue(element.getDateFin().toString());
			
			COTSFieldDataAndMD<?> salaireParAnneeRG = list.getFieldAndMDForPath("salaireRG/asCurrent");
			salaireParAnneeRG.setXMLValue(element.getToTalSalaire().toString());
			
			COTSFieldDataAndMD<?> cotistationRG = list.getFieldAndMDForPath("cotisationRg/asCurrent");
			cotistationRG.setXMLValue("");
			
			COTSFieldDataAndMD<?> pointsRG = list.getFieldAndMDForPath("pointsRG/asCurrent");
			pointsRG.setXMLValue(new BigDecimal(nbrePointRG).toString()); 
			nbreTotalPointsRG+=nbrePointRG;
		
			i++;
		}
		return nbreTotalPointsRG;
	}
	
	private int formCreatorRC(List<DetailCotisation> elements,BusinessObjectInstance boInstance) {

		//BusinessObjectInstance boInstance = BusinessObjectInstance.create(this.getFormType());

		COTSInstanceNode sectionRegimeCadre = boInstance.getGroup("carriereRCC");

		COTSFieldDataAndMD<BigDecimal> maxPk = sectionRegimeCadre.getFieldAndMDForPath("maxPk");
		maxPk.setValue(new BigDecimal(elements.size()));
		int i=1;
		int nbreTotalPointsRC=0;
		
		for(DetailCotisation element: elements){
			
			InformationCalcPoint info=getDataPoints(element.getDateDebut().getYear(), 2);
			int nbrePointRG=(int) (min(element.getToTalSalaire().intValue(), info.getPlafondSalAnnuel().intValue())*info.getTauxContractuel().doubleValue()/info.getSalaireRef().intValue());
			
			COTSInstanceListNode list = sectionRegimeCadre.getList("carriereRCCList").newChild();
			
			COTSFieldDataAndMD<BigDecimal> pk = list.getFieldAndMDForPath("pk");
			pk.setValue(new BigDecimal(i)); 
			
			COTSFieldDataAndMD<?> etablissement = list.getFieldAndMDForPath("etablissementRCC/asCurrent");
			etablissement.setXMLValue(element.getNomEmployeur());
			
			COTSFieldDataAndMD<?> DateDebut = list.getFieldAndMDForPath("anneeDuRCC/asCurrent");
			DateDebut.setXMLValue(element.getDateDebut().toString());
			
			COTSFieldDataAndMD<?> dateFin = list.getFieldAndMDForPath("AURCC/asCurrent");
			dateFin.setXMLValue(element.getDateFin().toString());
			
			COTSFieldDataAndMD<?> salaireParAnneeRC = list.getFieldAndMDForPath("salaireRCC/asCurrent");
			salaireParAnneeRC.setXMLValue(element.getToTalSalaire().toString());
			
			COTSFieldDataAndMD<?> cotistationRC = list.getFieldAndMDForPath("cotisationRCC/asCurrent");
			cotistationRC.setXMLValue("");
			
			COTSFieldDataAndMD<?> pointsRC = list.getFieldAndMDForPath("pointsRCC/asCurrent");
			pointsRC.setXMLValue(new BigDecimal(nbrePointRG).toString()); 
			nbreTotalPointsRC+=nbrePointRG;
		
			i++;
		}
		return nbreTotalPointsRC;
	}


}
