package com.splwg.cm.domain.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.todo.role.Role;
import com.splwg.base.domain.todo.role.Role_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author Divakar
 *
 * @BatchJob (modules = { }, softParameters = { @BatchJobSoftParameter (name =Days1, type = string) , @BatchJobSoftParameter (name = Days2, type= string) , @BatchJobSoftParameter (name = Days3, type = string)
 *           , @BatchJobSoftParameter (name = Days4, type = string)
 *           , @BatchJobSoftParameter (name = Days5, type = string)})
 */
public class CmControlProcessEmailTrigger extends CmControlProcessEmailTrigger_Gen {

	static Properties emailproperties = new Properties();

	{
		
		// Static Properties setting 
		
		emailproperties.put("mail.smtp.host", "smtp.office365.com");
		emailproperties.put("mail.smtp.port", "587");
		emailproperties.put("mail.smtp.auth", "true");
		emailproperties.put("mail.smtp.starttls.enable", "true");
        //properties.put("mail.smtp.ssl.trust", "smtp.office365.com");
		emailproperties.put("mail.smtp.ssl.trust", "*");
        
		/*emailproperties.put("mail.smtp.host", "smtp.gmail.com");
         // for Gmail Server.
		emailproperties.put("mail.smtp.port", "587");
		emailproperties.put("mail.smtp.auth", "true");
		emailproperties.put("mail.smtp.starttls.enable", "true");
		// properties.put("mail.smtp.ssl.trust", "smtp.office365.com");
		emailproperties.put("mail.smtp.ssl.trust", "*"); */

		
	}

	// creates a new session with an authenticator
	static Authenticator auth = new Authenticator() 
	{
		 public PasswordAuthentication getPasswordAuthentication()
         {
             return new PasswordAuthentication("psrmdev@secusociale.sn", "Passer123");
         }
	};

	private static final Logger logger = LoggerFactory.getLogger(CmControlProcessEmailTrigger.class);

	public JobWork getJobWork() {
		ThreadWorkUnit unit = new ThreadWorkUnit();
		List<ThreadWorkUnit> listOfThreadWorkUnit = new ArrayList<ThreadWorkUnit>();
		unit.addSupplementalData("maxErrors", this.getParameters().getSoftMaxErrors());
		listOfThreadWorkUnit.add(unit);

		JobWork jobWork = createJobWorkForThreadWorkUnitList(listOfThreadWorkUnit);
		System.out.println("######################## Terminate JobWorker ############################");
		return jobWork;
		// TODO Auto-generated method stub

	}

	public Class<CmControlProcessEmailTriggerWorker> getThreadWorkerClass() {
		return CmControlProcessEmailTriggerWorker.class;
	}

	public static class CmControlProcessEmailTriggerWorker extends CmControlProcessEmailTriggerWorker_Gen {

		String processId;
		private void noEmailExists(String userId) {
			//startChanges();
			// BusinessService_Id businessServiceId=new
			// BusinessService_Id("F1-AddToDoEntry");
			BusinessServiceInstance businessServiceInstance = BusinessServiceInstance.create("F1-AddToDoEntry");
			Role_Id toDoRoleId = new Role_Id("CM-CTRL1");
			Role toDoRole = toDoRoleId.getEntity();
			businessServiceInstance.getFieldAndMDForPath("sendTo").setXMLValue("SNDR");
			businessServiceInstance.getFieldAndMDForPath("subject")
					.setXMLValue("No Email available for the UserId " + userId + "");
			businessServiceInstance.getFieldAndMDForPath("toDoType").setXMLValue("CM-CENEM");
			businessServiceInstance.getFieldAndMDForPath("toDoRole").setXMLValue(toDoRole.getId().getTrimmedValue());
			businessServiceInstance.getFieldAndMDForPath("drillKey1").setXMLValue(""+processId+"");
			businessServiceInstance.getFieldAndMDForPath("messageCategory").setXMLValue("90001");
			businessServiceInstance.getFieldAndMDForPath("messageNumber").setXMLValue("1058");
			businessServiceInstance.getFieldAndMDForPath("messageParm1").setXMLValue(userId);
			//businessServiceInstance.getFieldAndMDForPath("sortKey1").setXMLValue("NEWTEST");
			BusinessServiceDispatcher.execute(businessServiceInstance);
			//saveChanges();

		}

		public ThreadExecutionStrategy createExecutionStrategy() {
				// TODO Auto-generated method stub
			return new CommitEveryUnitStrategy(this);
		}

		@SuppressWarnings("deprecation")
		public boolean executeWorkUnit(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			// TODO Auto-generated method stub
			
			

//			SimpleDateFormat ctrlDate = new SimpleDateFormat("dd/MM/yyyy"); 
			//DateFormat ctrlDate ;
			 //String ctrlDate = simDateFrm.format(ctrlDate);

			String dayStrDiff1 = this.getParameters().getDays1();
			String dayStrDiff2 = this.getParameters().getDays2();
			String dayStrDiff3 = this.getParameters().getDays3();
			String dayStrDiff4 = this.getParameters().getDays4();
			String dayStrDiff5 = this.getParameters().getDays5();

			ArrayList<Integer> dateList = new ArrayList<Integer>();

			if (!isNullOrBlank(dayStrDiff1)) {
				int date1 = Integer.parseInt(dayStrDiff1);
				dateList.add(date1);
			}
			if (!isNullOrBlank(dayStrDiff2)) {
				int date2 = Integer.parseInt(dayStrDiff2);
				dateList.add(date2);
			}
			if (!isNullOrBlank(dayStrDiff3)) {
				int date3 = Integer.parseInt(dayStrDiff3);
				dateList.add(date3);
			}
			if (!isNullOrBlank(dayStrDiff4)) {
				int date4 = Integer.parseInt(dayStrDiff4);
				dateList.add(date4);
			}
			if (!isNullOrBlank(dayStrDiff5)) {
				int date5 = Integer.parseInt(dayStrDiff1);
				dateList.add(date5);
			}

			Collections.sort(dateList, Collections.reverseOrder());

			for (int datelistobject : dateList) {

				String daysToTrigger = " sysdate + " + datelistobject;

				PreparedStatement psPreparedStatement = null;
				QueryIterator<SQLResultRow> result = null;
				
				psPreparedStatement = createPreparedStatement(" SELECT TO_CHAR (ctrl_date,'dd/MM/yyyy'), CTRL_USER_NAME, proc_flow_id, user_id from cm_controle_equipe  where CTRL_DATE = TRUNC("	+ daysToTrigger + ") ");
				logger.info(psPreparedStatement);
				result = psPreparedStatement.iterate();
				try {

					while (result.hasNext()) {
						SQLResultRow payDetailRow = result.next();
				      	String ctrlDate = payDetailRow.getString("TO_CHAR(CTRL_DATE,'DD/MM/YYYY')");
						processId = payDetailRow.getString("PROC_FLOW_ID");
						String userId = payDetailRow.getString("USER_ID");
						String userName = payDetailRow.getString("CTRL_USER_NAME");
                          //ctrlDate = simDateFrm.format(ctrlDate);
						// query to get Users ID

						PreparedStatement psPreparedStatementEmail = null;
						QueryIterator<SQLResultRow> resultEmail = null;

						psPreparedStatementEmail = createPreparedStatement(" select emailid from SC_USER where USER_ID =    \'" + userId + "\' ", "SELECT");
						resultEmail = psPreparedStatementEmail.iterate();
						SQLResultRow payDetailRowEmail = resultEmail.next();
						String emailId = payDetailRowEmail.getString("EMAILID");
						resultEmail.close();
						if (!isBlankOrNull(emailId)) {
							logger.info("email is " + emailId + "");
							Session session = Session.getInstance(emailproperties, auth);
							// creates a new e-mail message
							Message msg = new MimeMessage(session);
							msg.setFrom(new InternetAddress("psrmdev@secusociale.sn"));
							InternetAddress[] toAddresses = { new InternetAddress(emailId) };
							msg.setRecipients(Message.RecipientType.TO, toAddresses);
							msg.setSubject("Control Employer alert");
							msg.setSentDate(new Date());
							msg.setText("Bonjour " + userName + ", vous êtes affecté à une mission de contrôle à la date du " + ctrlDate + " soit dans "+datelistobject+"   ");
							// sends the e-mail
							System.out.print("Bonjour " + userName + ", vous êtes affecté à une mission de contrôle à la date du " + ctrlDate + " soit dans "+datelistobject+" )");
							Transport.send(msg);
						} else {
							logger.info("No email user id " + userId + "");

							 noEmailExists(userId);

						}
					}

				}

				catch (NullPointerException e) {
					// TODO Auto-generated catch block
					logger.info("no data Exist for the given Days value");
					e.printStackTrace();
				} catch (AddressException e) {
					// TODO Auto-generated catch block

					logger.info("invalid Email Address");
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					logger.info("unbale to send email.Problem in Sender email");
					e.printStackTrace();
				} finally {
					result.close();
					psPreparedStatement.close();

				}

			}

			
			return true;
		}

	}

}
