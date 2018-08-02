package com.splwg.cm.domain.admin.formRule;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.service.DataElement;
import com.splwg.base.domain.security.user.UserGroups;
import com.splwg.base.domain.security.userGroup.UserGroupUser;
import com.splwg.base.support.context.Session;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.cm.domain.customMessages.CmMessageRepository90007;

import com.splwg.tax.domain.customerinfo.serviceAgreement.ServiceAgreement_DTO; 

/**
 * @author Anita M
 *
@MaintenanceExtension (serviceName = CILCSVAP)
 */
public class CmObligationRestrictPopUp_Impl extends CmObligationRestrictPopUp_Impl_Gen {


	@Override
	public void afterRead(DataElement result) {

		UserGroups usergrp = null;
		ServiceAgreement_DTO saDTO = null;
		Session session = null;
		System.out.println("Program Starts");

		System.out.println(" Program Starts DataElement " + result);
		session = SessionHolder.getSession();
		if (session.isOnlineConnection()) {

			usergrp = (getActiveContextUser().getId()).getEntity().getGroups();

			System.out.println("User Group" + usergrp);
			saDTO = (ServiceAgreement_DTO) result.getPrimaryRow().getEntity().getDTO();

			System.out.println("Service Agreement" + saDTO);

			System.out.println("Service Agreement  Type " + saDTO.getServiceAgreementTypeId().getSaType());
			// Master Configuration to retrieve UserGroup List Against saType
			BusinessObjectInstance saTypeBo = BusinessObjectInstance.create("CM-SaTypUsrGrpMastrConfg");
			saTypeBo.set("bo", "CM-SaTypUsrGrpMastrConfg");
			saTypeBo = BusinessObjectDispatcher.read(saTypeBo);

			COTSInstanceList cotsSa = saTypeBo.getGroupFromPath("description").getList("usrGrpSaTypConfiglist");
			Map<String, ArrayList<String>> saGrp = new HashMap<String, ArrayList<String>>();
			String saTyp = null;
			ArrayList<String> usrGrplist = null;
			String saId = saDTO.getEntity().getId().toString();

			for (COTSInstanceListNode nod : cotsSa) {
				saTyp = nod.getXMLString("saTyp").trim();
				for (COTSInstanceListNode nod1 : nod.getList("usrGrpList")) {
					usrGrplist = saGrp.get(saTyp);
					if (isNull(usrGrplist)) {
						usrGrplist = new ArrayList<String>();
					}
					usrGrplist.add(nod1.getXMLString("usrgrp"));
				}
				saGrp.put(saTyp, usrGrplist);
			}

			if (!isNull(saGrp)) {

				for (Map.Entry<String, ArrayList<String>> saGroup : saGrp.entrySet()) {

					System.out.println("Key = " + saGroup.getKey() + ", Value = " + saGroup.getValue());

					if (saGroup.getKey().equalsIgnoreCase(saDTO.getServiceAgreementTypeId().getSaType().trim())) {

						for (UserGroupUser usrgrp : usergrp.asSet()) {

							if (saGroup.getValue().contains(usrgrp.fetchIdUserGroup().getId().getIdValue().trim())) {

								addError(CmMessageRepository90007.MSG_331(saId, saTyp, ""));

							}
						}
					}
				}
			}

			System.out.println("Program Ends");
		}
	}
}
