package com.splwg.cm.domain.admin.letterTemplate;


import java.math.BigInteger;



import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.report.reportDefinition.ReportDefinition;
import com.splwg.cm.domain.admin.letterTemplate.CmExtractPdf_Gen;
import com.splwg.tax.api.lookup.PrintPlatformLookup;
import com.splwg.tax.domain.admin.letterTemplate.LetterTemplate;
import com.splwg.tax.domain.admin.letterTemplate.LetterTemplateLetterExtractAlgorithmSpot;
import com.splwg.tax.domain.common.fileHelper.FileHelper;
import com.splwg.tax.domain.customerinfo.customerContact.CustomerContact;



/**
 * @author ayoub.damir
 *
@AlgorithmComponent ()
 */
public class CmExtractPdf_Impl extends CmExtractPdf_Gen implements
		LetterTemplateLetterExtractAlgorithmSpot {

    private CustomerContact vCustomerContact;


    private ReportDefinition vReportDefinition;


    private PrintPlatformLookup vPrintPlatformLookup;



    public ReportDefinition getLetterPrintReport() {

        return this.vReportDefinition;
    }


    public PrintPlatformLookup getPrintPlatform() {
        return this.vPrintPlatformLookup;
    }
    
	@Override
	public void invoke() {}

	@Override
	public BigInteger getCustomerContactExtractedCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCustomerContact(CustomerContact paramCustomerContact) {
		// TODO Auto-generated method stub
		vCustomerContact = paramCustomerContact;
	}

	@Override
	public void setCustomerContactExtractedCount(BigInteger arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setExtractFileName(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFileHelper(FileHelper arg0) {
		// TODO Auto-generated method stub

	}
    
    
	@Override
	public void setIsFieldDelimited(Bool arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsFirstHeaderDelimited(Bool arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLetterTemplate(LetterTemplate arg0) {
		// TODO Auto-generated method stub

	}
	
    }
