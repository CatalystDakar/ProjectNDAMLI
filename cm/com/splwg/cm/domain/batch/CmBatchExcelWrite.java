package com.splwg.cm.domain.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.cm.domain.batch.CmStageUploadDTO;

/**
 * @author Balaganesh M
 *
@BatchJob (modules={},softParameters = { @BatchJobSoftParameter (name = filePath, required = true, type = string)})
 */
public class CmBatchExcelWrite extends CmBatchExcelWrite_Gen {

	private final static Logger log = LoggerFactory.getLogger(CmBatchExcelWrite.class);

	@Override
	public void validateSoftParameters(boolean isNewRun) {
		System.out.println("File path: " + this.getParameters().getFilePath());
	}
	
	public JobWork getJobWork() {
		List<ThreadWorkUnit> threadWorkUnits = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit th = null;

		th = new ThreadWorkUnit();
		th.addSupplementalData("filePath", this.getParameters().getFilePath());
		threadWorkUnits.add(th);

		return createJobWorkForThreadWorkUnitList(threadWorkUnits);
	}

	public Class<CmBatchExcelWriteWorker> getThreadWorkerClass() {
		return CmBatchExcelWriteWorker.class;
	}

	public static class CmBatchExcelWriteWorker extends CmBatchExcelWriteWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			return new CommitEveryUnitStrategy(this);
		}

		@SuppressWarnings("deprecation")
		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {

			String filePath = this.getParameters().getFilePath();
			String FILE_NAME = null;
			PreparedStatement psPreparedStatement = null;
			Map<String,List<CmStageUploadDTO>> hmStageUploadMap = new HashMap<String,List<CmStageUploadDTO>>();
	        Set<Integer> setBold = new HashSet<Integer> ();
	        setBold.add(1);
	        setBold.add(4);
	        setBold.add(6);
	        setBold.add(7);
	        Set<Integer> setUnderline = new HashSet<Integer> ();
	        setUnderline.add(0);
	        setUnderline.add(3);
	        setUnderline.add(6);
	        
			if(!isBlankOrNull(filePath)){
		        CmStageUploadDTO cmStageUploadDTO = null;
		        List<CmStageUploadDTO> cmExcelDTOList = null;
		        QueryIterator<SQLResultRow> result = null;
		        startChanges();
				psPreparedStatement = createPreparedStatement(CmDNSConstant.STAGING_FILE_GEN_QUERY);
				try {
					result = psPreparedStatement.iterate();
					while (result.hasNext()){
						SQLResultRow resultValues = result.next();
						cmStageUploadDTO = new CmStageUploadDTO();
						String immatriculationIdPeriod = null;
						cmExcelDTOList = new ArrayList<CmStageUploadDTO>();
						cmStageUploadDTO.setRaisonSociale(resultValues.getString("RAISON_SOCIALE"));
						cmStageUploadDTO.setAdresse(resultValues.getString("ADRESSE"));
						cmStageUploadDTO.setImmatriculationId(resultValues.getString("ID_IMMATRICULATION"));
						cmStageUploadDTO.setNineaId(resultValues.getString("NINEA"));
						//
						cmStageUploadDTO.setDateDebutPeriod(resultValues.getDate("DATE_DEBUT_PERIODE_COTISATION"));
						cmStageUploadDTO.setDateFinPeriod(resultValues.getDate("DATE_FIN_PERIODE_COTISATION"));
						cmStageUploadDTO.setTotalNauxSalaries(resultValues.getString("TOTAL_NOUVEAUX_SALARIES"));
						cmStageUploadDTO.setTotalSalaries(resultValues.getString("TOTAL_SALARIES"));
						cmStageUploadDTO.setMontDeCotPF(resultValues.getString("MONT_DE_COTISATION_PF"));
						cmStageUploadDTO.setMontDeCotAtmp(resultValues.getString("MONT_DE_COTISATION_ATMP"));
						cmStageUploadDTO.setMontDeCotRetPF(resultValues.getString("MONT_DE_COTISATION_RET_PF"));
						cmStageUploadDTO.setMontDeCotRetAtmp(resultValues.getString("MONT_DE_COTISATION_RET_ATMP"));
						cmStageUploadDTO.setSynTotalSalIpresRg(resultValues.getString("SYN_TOTAL_SAL_PLAF_IPRES_RG"));
						cmStageUploadDTO.setSynTotalSalIpresRcc(resultValues.getString("SYN_TOTAL_SAL_PLAF_IPRES_RCC"));
						cmStageUploadDTO.setSynTotalSalCssPf(resultValues.getString("SYN_TOTAL_SAL_PLAF_CSS_PF"));
						cmStageUploadDTO.setSynTotalSalCssAtmp(resultValues.getString("SYN_TOTAL_SAL_PLAF_CSS_ATMP"));
						cmStageUploadDTO.setSynTalSalVerses(resultValues.getString("SYN_TAL_SAL_VERSES"));
						cmStageUploadDTO.setNumeroAssureSocial(resultValues.getString("NUMERO_ASSURE_SOCIAL"));
						cmStageUploadDTO.setTypePiece(resultValues.getString("TYPE_PIECE"));
						cmStageUploadDTO.setNumeroPiece(resultValues.getString("NUMERO_PIECE"));
						cmStageUploadDTO.setNom(resultValues.getString("NOM"));
						cmStageUploadDTO.setPrenom(resultValues.getString("PRENOM"));
						if(!isNull(resultValues.getString("DATE_NAISSANCE"))){
							String birthDateStr = resultValues.getString("DATE_NAISSANCE");
							SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy");
							Date birthDate = sdf.parse(birthDateStr);
							cmStageUploadDTO.setDateDeNaissance(birthDate);
						}
						cmStageUploadDTO.setRegime(resultValues.getString("REGIME"));
						cmStageUploadDTO.setTotalSalCssPf1(resultValues.getString("TOTAL_SAL_CSS_PF1"));
						cmStageUploadDTO.setTotalSalCssAtmp1(resultValues.getString("TOTAL_SAL_CSS_ATMP1"));
						cmStageUploadDTO.setTotalSalIpresRg1(resultValues.getString("TOTAL_SAL_IPRES_RG1"));
						cmStageUploadDTO.setTotalSalIpresRcc1(resultValues.getString("TOTAL_SAL_IPRES_RCC1"));
						cmStageUploadDTO.setSalarieReel1(resultValues.getString("SALAIRE_REEL_BRUT_PERCU1"));
						cmStageUploadDTO.setTempsPresenceJour1(resultValues.getString("TEMPS_PRESENCE_JOUR1"));
						cmStageUploadDTO.setTempsPresenceHeures1(resultValues.getString("TEMPS_PRESENCE_HEURES1"));
						cmStageUploadDTO.setTempsTravail1(resultValues.getString("TEMPS_DE_TRAVAIL1"));
						cmStageUploadDTO.setTrancheTravail1(resultValues.getString("TRANCHE_TRAVAIL1"));
						cmStageUploadDTO.setDateEffectRegime1(resultValues.getDate("DATE_EFFET_REGIME1")); 
						cmStageUploadDTO.setRcc1(resultValues.getString("RCC1"));
						cmStageUploadDTO.setRg1(resultValues.getString("RG1"));
						cmStageUploadDTO.setTotalSalCssPf2(resultValues.getString("TOTAL_SAL_CSS_PF2"));
						cmStageUploadDTO.setTotalSalCssAtmp2(resultValues.getString("TOTAL_SAL_CSS_ATMP2"));
						cmStageUploadDTO.setTotalSalIpresRg2(resultValues.getString("TOTAL_SAL_IPRES_RG2"));
						cmStageUploadDTO.setTotalSalIpresRcc2(resultValues.getString("TOTAL_SAL_IPRES_RCC2"));
						cmStageUploadDTO.setSalarieReel2(resultValues.getString("SALAIRE_REEL_BRUT_PERCU2"));
						cmStageUploadDTO.setTempsPresenceJour2(resultValues.getString("TEMPS_PRESENCE_JOUR2"));
						cmStageUploadDTO.setTempsPresenceHeures2(resultValues.getString("TEMPS_PRESENCE_HEURES2"));
						cmStageUploadDTO.setTempsTravail2(resultValues.getString("TEMPS_DE_TRAVAIL2"));
						cmStageUploadDTO.setTrancheTravail2(resultValues.getString("TRANCHE_TRAVAIL2"));
						cmStageUploadDTO.setDateEffectRegime2(resultValues.getDate("DATE_EFFET_REGIME2")); 
						cmStageUploadDTO.setRcc2(resultValues.getString("RCC2"));
						cmStageUploadDTO.setRg2(resultValues.getString("RG2"));
						cmStageUploadDTO.setTotalSalCssPf3(resultValues.getString("TOTAL_SAL_CSS_PF3"));
						cmStageUploadDTO.setTotalSalCssAtmp3(resultValues.getString("TOTAL_SAL_CSS_ATMP3"));
						cmStageUploadDTO.setTotalSalIpresRg3(resultValues.getString("TOTAL_SAL_IPRES_RG3"));
						cmStageUploadDTO.setTotalSalIpresRcc3(resultValues.getString("TOTAL_SAL_IPRES_RCC3"));
						cmStageUploadDTO.setSalarieReel3(resultValues.getString("SALAIRE_REEL_BRUT_PERCU3"));
						cmStageUploadDTO.setTempsPresenceJour3(resultValues.getString("TEMPS_PRESENCE_JOUR3"));
						cmStageUploadDTO.setTempsPresenceHeures3(resultValues.getString("TEMPS_PRESENCE_HEURES3"));
						cmStageUploadDTO.setTempsTravail3(resultValues.getString("TEMPS_DE_TRAVAIL3"));
						cmStageUploadDTO.setTrancheTravail3(resultValues.getString("TRANCHE_TRAVAIL3"));
						cmStageUploadDTO.setDateEffectRegime3(resultValues.getDate("DATE_EFFET_REGIME3")); 
						cmStageUploadDTO.setRcc3(resultValues.getString("RCC3"));
						cmStageUploadDTO.setRg3(resultValues.getString("RG3"));
						cmStageUploadDTO.setContractType(resultValues.getString("TYPE_CONTRAT"));
						cmStageUploadDTO.setEntreeDate(resultValues.getDate("DATE_ENTREE"));
						cmStageUploadDTO.setSortieDate(resultValues.getDate("DATE_SORTIE"));
						cmStageUploadDTO.setMotifSortie(resultValues.getString("MOTIF_SORTIE"));
						cmStageUploadDTO.setTravailleurId(resultValues.getString("ID_TRAVAILLEUR"));
						
						
						cmExcelDTOList.add(cmStageUploadDTO);
						immatriculationIdPeriod = cmStageUploadDTO.getImmatriculationId()+"_"+(cmStageUploadDTO.getDateDebutPeriod()!= null?cmStageUploadDTO.getDateDebutPeriod():CmDNSConstant.BLANK)+"_"+(cmStageUploadDTO.getDateFinPeriod()!= null?cmStageUploadDTO.getDateFinPeriod():CmDNSConstant.BLANK);
						if(hmStageUploadMap.isEmpty()){
							hmStageUploadMap.put(immatriculationIdPeriod, cmExcelDTOList);
						}else{
							if(hmStageUploadMap.containsKey(immatriculationIdPeriod)){
								List<CmStageUploadDTO> stageUploadDTOList = hmStageUploadMap.get(immatriculationIdPeriod);
								stageUploadDTOList.add(cmStageUploadDTO);
								hmStageUploadMap.put(immatriculationIdPeriod, stageUploadDTOList);
							}else{
								hmStageUploadMap.put(immatriculationIdPeriod,cmExcelDTOList);
							}
						}
					}
					saveChanges();
				} catch (Exception exception) {
					log.error("Exception in Batch Excel write: "+exception);
				} finally {
					psPreparedStatement.close();
					result.close();
					psPreparedStatement = null;
				}
				
				for (Map.Entry<String, List<CmStageUploadDTO>> stgUploadMap : hmStageUploadMap.entrySet())
				{
			        XSSFWorkbook workbook = new XSSFWorkbook();
			        XSSFSheet sheet = workbook.createSheet("DNS");
			        XSSFCellStyle style = workbook.createCellStyle();
			        XSSFFont font= workbook.createFont();
			        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			        style.setFont(font);
			        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
			        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
			        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			        XSSFCellStyle style1 = workbook.createCellStyle();
			        XSSFFont font1= workbook.createFont();
			        font1.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			        font1.setFontHeight(19.0);
			        font1.setUnderline(XSSFFont.U_SINGLE);
			        style1.setFont(font1);
			        XSSFCellStyle style2 = workbook.createCellStyle();
			        style2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			        style2.setBorderTop(XSSFCellStyle.BORDER_THIN);
			        style2.setBorderRight(XSSFCellStyle.BORDER_THIN);
			        style2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			        Object[][] excelObject = null;
					int listSize = stgUploadMap.getValue().size()+10;
					excelObject = new Object [listSize][46];
					int rowNum = 0,rowNumber = 0,colNum = 0,count =0;
					
					for(CmStageUploadDTO cmStageDTO : stgUploadMap.getValue()){
						if(count == 0){
							excelObject[rowNum][colNum] = CmDNSConstant.INFORMATION_EMPLOYEUR;
							rowNum++;//Employer Headers
							excelObject[rowNum][colNum] = CmDNSConstant.TYPE_IDENTIFIANT;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.NUMERO_IDENTIFANT;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RAISON_SOCIALE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.ADDRESS;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TYPE_DE_DECLARATION;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_DEBUT_PERIOD;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_FIN_PERIOD;colNum++;
							//excelObject[rowNum][colNum] = CmDNSConstant.ACTIVITE_PRINCIPALE;colNum++;

							rowNum++;colNum=0; //Employer Values
							excelObject[rowNum][colNum] = CmDNSConstant.SCI;colNum++;// cmStageDTO.getTypePiece()!=null?cmStageDTO.getTypePiece():"";colNum++;
							//if(cmStageDTO.getTypePiece().equalsIgnoreCase("SCI")){
							excelObject[rowNum][colNum] = cmStageDTO.getNineaId()!=null?cmStageDTO.getNineaId():"";colNum++;
							//}else{
								//excelObject[rowNum][colNum] = cmStageDTO.getNumeroPiece()!=null?cmStageDTO.getNumeroPiece():"";colNum++;
							//}
							excelObject[rowNum][colNum] = cmStageDTO.getRaisonSociale()!=null?cmStageDTO.getRaisonSociale():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getAdresse()!=null?cmStageDTO.getAdresse():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTypeDeDeclaration()!=null?cmStageDTO.getTypeDeDeclaration():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateDebutPeriod()!=null?cmStageDTO.getDateDebutPeriod():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateFinPeriod()!=null?cmStageDTO.getDateFinPeriod():"";colNum++;
							//excelObject[rowNum][colNum] = cmStageDTO.getActivatePrincipale()!=null?cmStageDTO.getActivatePrincipale():"";colNum++;
							//excelObject[rowNum][colNum] = cmStageDTO.getImmatriculationId();colNum++;
							//immatriculationId = cmStageDTO.getImmatriculationId();
							//excelObject[rowNum][colNum] = cmStageDTO.getNineaId()!=null?cmStageDTO.getNineaId():"";colNum++;
							

							rowNum++;colNum=0; //Synthese
							excelObject[rowNum][colNum] = CmDNSConstant.SYNTHESE;
							rowNum++; //Synthese Headers
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_NOUVEUX_SALARIES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_ATMP;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIES_VERSES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.MONTANT_DE_COTISATION_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.MONTANT_DE_COTISATION_ATMP;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.MONTANT_DE_COTISATION_RET_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.MONTANT_DE_COTISATION_RET_ATMP;colNum++;
							rowNum++;colNum=0; //Synthese Values
							excelObject[rowNum][colNum] = cmStageDTO.getTotalNauxSalaries()!=null?cmStageDTO.getTotalNauxSalaries():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalaries()!=null?cmStageDTO.getTotalSalaries():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSynTotalSalIpresRg()!=null?cmStageDTO.getSynTotalSalIpresRg():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSynTotalSalIpresRcc()!=null?cmStageDTO.getSynTotalSalIpresRcc():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSynTotalSalCssPf()!=null?cmStageDTO.getSynTotalSalCssPf():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSynTotalSalCssAtmp()!=null?cmStageDTO.getSynTotalSalCssAtmp():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSynTalSalVerses()!=null?cmStageDTO.getSynTalSalVerses():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMontDeCotPF()!=null?cmStageDTO.getMontDeCotPF():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMontDeCotAtmp()!=null?cmStageDTO.getMontDeCotAtmp():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMontDeCotRetPF()!=null?cmStageDTO.getMontDeCotRetPF():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMontDeCotRetAtmp()!=null?cmStageDTO.getMontDeCotRetAtmp():"";colNum++;
							rowNum++;colNum=0; // Informations des salariés
							excelObject[rowNum][colNum] = CmDNSConstant.INFORMATION_SALARIES;colNum=10;
							excelObject[rowNum][colNum] = CmDNSConstant.MOIS1;colNum=22;
							excelObject[rowNum][colNum] = CmDNSConstant.MOIS2;colNum=34;
							excelObject[rowNum][colNum] = CmDNSConstant.MOIS3;
							rowNum++;colNum=0; //Informations des salariés Headers
							excelObject[rowNum][colNum] = CmDNSConstant.NUMERO_ASSURE_SOCIAL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.NOM;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.PRENOM;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_DE_NASISSANCE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TYPE_PIECE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.NUMERO_PIECE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TYPE_DE_CONTRACT;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_ENTREE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_SORTIE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.MOTIF_SORTIE;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_ATMP;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.SALARIE_REEL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_JOURS;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_HEURES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TRANCHE_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_REGIME;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_ATMP;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.SALARIE_REEL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_JOURS;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_HEURES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TRANCHE_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_REGIME;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_PF;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_CSS_ATMP;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TOTAL_SALARIE_IPRES_RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.SALARIE_REEL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_JOURS;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_HEURES;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TEMPS_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.TRANCHE_TRAVAIL;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RG;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.RCC;colNum++;
							excelObject[rowNum][colNum] = CmDNSConstant.DATE_REGIME;colNum++;
							
							rowNum++;colNum=0; // Informations des salariés values
							excelObject[rowNum][colNum] = cmStageDTO.getNumeroAssureSocial()!=null?cmStageDTO.getNumeroAssureSocial():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getNom()!=null?cmStageDTO.getNom():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getPrenom()!=null?cmStageDTO.getPrenom():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateDeNaissance()!=null?cmStageDTO.getDateDeNaissance():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTypePiece()!=null?cmStageDTO.getTypePiece():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getNumeroPiece()!=null?cmStageDTO.getNumeroPiece():"";colNum++;
							//excelObject[rowNum][colNum] = cmStageDTO.getRegime()!=null?cmStageDTO.getRegime():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getContractType()!=null?cmStageDTO.getContractType():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getEntreeDate()!=null?cmStageDTO.getEntreeDate():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSortieDate()!=null?cmStageDTO.getSortieDate():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMotifSortie()!=null?cmStageDTO.getMotifSortie():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf1()!=null?cmStageDTO.getTotalSalCssPf1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp1()!=null?cmStageDTO.getTotalSalCssAtmp1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg1()!=null?cmStageDTO.getTotalSalIpresRg1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc1()!=null?cmStageDTO.getTotalSalIpresRcc1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel1()!=null?cmStageDTO.getSalarieReel1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour1()!=null?cmStageDTO.getTempsPresenceJour1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures1()!=null?cmStageDTO.getTempsPresenceHeures1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail1()!=null?cmStageDTO.getTempsTravail1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail1()!=null?cmStageDTO.getTrancheTravail1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg1()!=null?cmStageDTO.getRg1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc1()!=null?cmStageDTO.getRcc1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime1()!=null?cmStageDTO.getDateEffectRegime1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf2()!=null?cmStageDTO.getTotalSalCssPf2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp2()!=null?cmStageDTO.getTotalSalCssAtmp2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg2()!=null?cmStageDTO.getTotalSalIpresRg2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc2()!=null?cmStageDTO.getTotalSalIpresRcc2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel2()!=null?cmStageDTO.getSalarieReel2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour2()!=null?cmStageDTO.getTempsPresenceJour2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures2()!=null?cmStageDTO.getTempsPresenceHeures2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail2()!=null?cmStageDTO.getTempsTravail2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail2()!=null?cmStageDTO.getTrancheTravail2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg2()!=null?cmStageDTO.getRg2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc2()!=null?cmStageDTO.getRcc2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime2()!=null?cmStageDTO.getDateEffectRegime2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf3()!=null?cmStageDTO.getTotalSalCssPf3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp3()!=null?cmStageDTO.getTotalSalCssAtmp3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg3()!=null?cmStageDTO.getTotalSalIpresRg3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc3()!=null?cmStageDTO.getTotalSalIpresRcc3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel3()!=null?cmStageDTO.getSalarieReel3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour3()!=null?cmStageDTO.getTempsPresenceJour3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures3()!=null?cmStageDTO.getTempsPresenceHeures3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail3()!=null?cmStageDTO.getTempsTravail3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail3()!=null?cmStageDTO.getTrancheTravail3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg3()!=null?cmStageDTO.getRg3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc3()!=null?cmStageDTO.getRcc3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime3()!=null?cmStageDTO.getDateEffectRegime3():"";colNum++;
						}else{
							rowNum++;colNum=0; // Informations des salariés values
							excelObject[rowNum][colNum] = cmStageDTO.getNumeroAssureSocial()!=null?cmStageDTO.getNumeroAssureSocial():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getNom()!=null?cmStageDTO.getNom():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getPrenom()!=null?cmStageDTO.getPrenom():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateDeNaissance()!=null?cmStageDTO.getDateDeNaissance():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTypePiece()!=null?cmStageDTO.getTypePiece():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getNumeroPiece()!=null?cmStageDTO.getNumeroPiece():"";colNum++;
							//excelObject[rowNum][colNum] = cmStageDTO.getRegime()!=null?cmStageDTO.getRegime():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getContractType()!=null?cmStageDTO.getContractType():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getEntreeDate()!=null?cmStageDTO.getEntreeDate():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSortieDate()!=null?cmStageDTO.getSortieDate():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getMotifSortie()!=null?cmStageDTO.getMotifSortie():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf1()!=null?cmStageDTO.getTotalSalCssPf1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp1()!=null?cmStageDTO.getTotalSalCssAtmp1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg1()!=null?cmStageDTO.getTotalSalIpresRg1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc1()!=null?cmStageDTO.getTotalSalIpresRcc1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel1()!=null?cmStageDTO.getSalarieReel1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour1()!=null?cmStageDTO.getTempsPresenceJour1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures1()!=null?cmStageDTO.getTempsPresenceHeures1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail1()!=null?cmStageDTO.getTempsTravail1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail1()!=null?cmStageDTO.getTrancheTravail1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg1()!=null?cmStageDTO.getRg1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc1()!=null?cmStageDTO.getRcc1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime1()!=null?cmStageDTO.getDateEffectRegime1():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf2()!=null?cmStageDTO.getTotalSalCssPf2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp2()!=null?cmStageDTO.getTotalSalCssAtmp2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg2()!=null?cmStageDTO.getTotalSalIpresRg2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc2()!=null?cmStageDTO.getTotalSalIpresRcc2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel2()!=null?cmStageDTO.getSalarieReel2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour2()!=null?cmStageDTO.getTempsPresenceJour2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures2()!=null?cmStageDTO.getTempsPresenceHeures2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail2()!=null?cmStageDTO.getTempsTravail2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail2()!=null?cmStageDTO.getTrancheTravail2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg2()!=null?cmStageDTO.getRg2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc2()!=null?cmStageDTO.getRcc2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime2()!=null?cmStageDTO.getDateEffectRegime2():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssPf3()!=null?cmStageDTO.getTotalSalCssPf3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalCssAtmp3()!=null?cmStageDTO.getTotalSalCssAtmp3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRg3()!=null?cmStageDTO.getTotalSalIpresRg3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTotalSalIpresRcc3()!=null?cmStageDTO.getTotalSalIpresRcc3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getSalarieReel3()!=null?cmStageDTO.getSalarieReel3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceJour3()!=null?cmStageDTO.getTempsPresenceJour3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsPresenceHeures3()!=null?cmStageDTO.getTempsPresenceHeures3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTempsTravail3()!=null?cmStageDTO.getTempsTravail3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getTrancheTravail3()!=null?cmStageDTO.getTrancheTravail3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRg3()!=null?cmStageDTO.getRg3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getRcc3()!=null?cmStageDTO.getRcc3():"";colNum++;
							excelObject[rowNum][colNum] = cmStageDTO.getDateEffectRegime3()!=null?cmStageDTO.getDateEffectRegime3():"";colNum++;
						}
						count++;
					}
					  for (Object[] excelType : excelObject) {
				            Row row = sheet.createRow(rowNumber++);
				            int columnNumber = 0;
				            for (Object field : excelType) {
				                Cell cell = row.createCell(columnNumber++);
				                if (field instanceof String) {
				                    cell.setCellValue((String) field);
				                    if(setBold.contains(row.getRowNum())){
				                    	cell.setCellStyle(style);
				                    }
				                    if(setUnderline.contains(row.getRowNum())){
				                    	cell.setCellStyle(style1);
				                    }
				                    if(!setUnderline.contains(row.getRowNum()) && !setBold.contains(row.getRowNum())){
				                    	 cell.setCellStyle(style2);
				                    }
				                    
				                } else if (field instanceof Integer) {
				                    cell.setCellValue((Integer) field);
				                    if(!setUnderline.contains(row.getRowNum()) && !setBold.contains(row.getRowNum())){
				                    	 cell.setCellStyle(style2);
				                    }
				                   
				                } else if (field instanceof Date) {
				                	java.text.DateFormat sf = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
				                	//DateFormat sf1 = new DateFormat("dd/MM/yyyy");
				                	Date dat = null;
				                	try {
										String date = sf.format((Date)field);
										dat = sf.parse(date);
									} catch (ParseException e) {
										e.printStackTrace();
									}
				                	XSSFCellStyle cellStyle = workbook.createCellStyle();
							        XSSFCreationHelper createHelper = workbook.getCreationHelper();
							        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
							        XSSFCellStyle combined = workbook.createCellStyle();
							        combined.cloneStyleFrom(style2);
							        combined.setDataFormat(cellStyle.getDataFormat());
				                	cell.setCellValue(dat);
				                	cell.setCellStyle(combined);
				                } else if (field instanceof com.splwg.base.api.datatypes.Date){
				                	java.text.DateFormat sf = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
				                	DateFormat sf1 = new DateFormat("dd/MM/yyyy");
				                	Date dat = null;
				                	try {
										String date = sf1.format((com.splwg.base.api.datatypes.Date) field);
										dat = sf.parse(date);
									} catch (ParseException e) {
										e.printStackTrace();
									}
				                	XSSFCellStyle cellStyle = workbook.createCellStyle();
							        XSSFCreationHelper createHelper = workbook.getCreationHelper();
							        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
							        XSSFCellStyle combined = workbook.createCellStyle();
							        combined.cloneStyleFrom(style2);
							        combined.setDataFormat(cellStyle.getDataFormat());
				                	cell.setCellValue(dat);
				                	cell.setCellStyle(combined);
				                }
				            }
				        }
					  
					  Row row= sheet.getRow(6);
					  XSSFCellStyle cellStyle = workbook.createCellStyle();
					  cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
					  cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
					  cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
					  cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
					  cellStyle.setFont(font);
					  cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					  for(int i=0;i<row.getLastCellNum();i++){
						  if(i>=10){
							 Cell cell =  row.getCell(i);
							 cell.setCellStyle(cellStyle);
						  }
					  }
					  
					  sheet.addMergedRegion(CellRangeAddress.valueOf("K7:V7"));
					  sheet.addMergedRegion(CellRangeAddress.valueOf("W7:AH7"));
					  sheet.addMergedRegion(CellRangeAddress.valueOf("AI7:AT7"));
				        try {
				        	FILE_NAME = null;
				        	FILE_NAME = filePath+"ImmatId_"+stgUploadMap.getKey()+".xlsx";
				        	File yourFile = new File(FILE_NAME); 
				        	yourFile.createNewFile(); // if file already exists will do nothing 
				        	FileOutputStream oFile = new FileOutputStream(yourFile, false); 
				            //FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
				            workbook.write(oFile);
				           // workbook.close();
				        } catch (FileNotFoundException e) {
				            e.printStackTrace();
				        } catch (IOException e) {
				            e.printStackTrace();
				        }
				}
				log.info("#####FILE CREATION DONE#####");
		    }
			return true;
		}
	}
}
