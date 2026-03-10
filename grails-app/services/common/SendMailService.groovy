package common


import grails.gorm.transactions.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import recruitment.AWSCredential

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Transactional
class SendMailService
{
   // boolean transactional = false
    def serviceMethod() {
    }
    // def SendMailService sendMailService
    // sendMailService.sendmail("volp.vu@gmail.com","volp@108","deepak.pawar@vit.edu","This is final tesing","hi hardworking guy","", "")
    def sendmail(String frommail,String frommailpassword,String sendto,String subject,String bodymessage,String attachmentfile, def cc_email)
    {
        if(sendto && sendto != 'director@vit.edu') {
            //d:/trust_office_ip.png
            final String username = frommail
            final String password = frommailpassword;
            //println("username >>"+ username)
            //println("password >>"+ password)
            def allsession = getSession()
            def host="smtp.gmail.com"
            def port="587"
            if(allsession?.smtp_host && allsession?.smtp_host!="NA")
             host=allsession?.smtp_host
            if(allsession?.smtp_port && allsession?.smtp_port!="NA")
             port= allsession?.smtp_port

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(frommail));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(sendto));

                if (cc_email != "") {
                    message.setRecipients(Message.RecipientType.CC,
                            InternetAddress.parse(cc_email))
                }

                message.setSubject(subject);
                if (attachmentfile.equals("")) {
                    message.setText(bodymessage);
                } else {
                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();
                    // Now set the actual message
                    messageBodyPart.setText(bodymessage);
                    // Create a multipar message
                    Multipart multipart = new MimeMultipart();
                    // Set text message part
                    multipart.addBodyPart(messageBodyPart);

                    // Part two is attachment
                    messageBodyPart = new MimeBodyPart();
                    String filename = attachmentfile
                    DataSource source = new FileDataSource(filename);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(filename);
                    multipart.addBodyPart(messageBodyPart);
                    // Send the complete message parts
                    message.setContent(multipart);
                }
                Transport.send(message);
                System.out.println("Mail Sent Successfully.....");
                return 1
            } catch (MessagingException e) {
                // throw new RuntimeException(e);
                System.out.println("Error::Mail NOT Sent....");
                return 0
            }
        }
    }

    def sendmailwithcss(String frommail,String frommailpassword,String sendto,String subject,String bodymessage,String attachmentfile, def cc_email) {
        if(sendto && sendto != 'director@vit.edu') {
            //d:/trust_office_ip.png
            final String username = frommail
            final String password = frommailpassword;

            def allsession = getSession()
            def host="smtp.gmail.com"
            def port="587"
            if(allsession?.smtp_host && allsession?.smtp_host!="NA")
                host=allsession?.smtp_host
            if(allsession?.smtp_port && allsession?.smtp_port!="NA")
                port= allsession?.smtp_port

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.host",host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(frommail));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(sendto));

                if (cc_email) {
                    message.setRecipients(Message.RecipientType.CC,
                            InternetAddress.parse(cc_email))
                }

                message.setSubject(subject);
                if (attachmentfile.equals("")) {
                    message.setContent(bodymessage, "text/html");
                }
                else {
                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();

                    // Now set the actual message
                    messageBodyPart.setContent(bodymessage, "text/html");

                    // Create a multipar message
                    Multipart multipart = new MimeMultipart();

                    // Set text message part
                    multipart.addBodyPart(messageBodyPart);

                    // Part two is attachment
                    messageBodyPart = new MimeBodyPart();
                    String filename = attachmentfile
                    DataSource source = new FileDataSource(filename);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(filename);
                    multipart.addBodyPart(messageBodyPart);
                    // Send the complete message parts
                    message.setContent(multipart);
                }
                Transport.send(message);
                System.out.println("Mail Sent Successfully.....");
                return 1
            }
            catch (MessagingException e) {
                // throw new RuntimeException(e);
                System.out.println("Error::Mail NOT Sent...." + e);
                return 0
            }
        }
    }

    def sendmailwithcssattachment(String frommail,String frommailpassword,String sendto,String subject,String bodymessage,def attachmentfile, def cc_email) {
        println("sendmailwithcssattachment ")
        if(sendto && sendto != 'director@vit.edu') {
            //d:/trust_office_ip.png
            final String username = frommail
            final String password = frommailpassword;

            def allsession = getSession()
            def host="smtp.gmail.com"
            def port="587"
            if(allsession?.smtp_host && allsession?.smtp_host!="NA")
                host=allsession?.smtp_host
            if(allsession?.smtp_port && allsession?.smtp_port!="NA")
                port= allsession?.smtp_port

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.host",host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(frommail));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(sendto));

                if (cc_email) {
                    message.setRecipients(Message.RecipientType.CC,
                            InternetAddress.parse(cc_email))
                }

                message.setSubject(subject);
                Multipart multipart = new MimeMultipart();

                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(bodymessage, "text/html");
                multipart.addBodyPart(messageBodyPart);

                if(attachmentfile?.size() > 0) {
                    messageBodyPart = new MimeBodyPart();
                    FileDataSource source = null;
                    for(item in attachmentfile) {
                        messageBodyPart = null;
                        source = null;
                        messageBodyPart = new MimeBodyPart();
                        source = new FileDataSource(item?.file);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(item?.name);
                        multipart.addBodyPart(messageBodyPart);
                    }
                }

                message.setContent(multipart);
                Transport.send(message);
                System.out.println("Mail Sent Successfully.....");
                return 1
            }
            catch (MessagingException e) {
                // throw new RuntimeException(e);
                System.out.println("Error::Mail NOT Sent...." + e);
                return 0
            }
        }
    }

    def sendmailwithcssBCC(String frommail,String frommailpassword,String sendto,String subject,String bodymessage,String attachmentfile, def cc_email,def bcc_email) {
        if(sendto && sendto != 'director@vit.edu') {
            //d:/trust_office_ip.png
            final String username = frommail
            final String password = frommailpassword;

            def allsession = getSession()
            def host="smtp.gmail.com"
            def port="587"
            if(allsession?.smtp_host && allsession?.smtp_host!="NA")
                host=allsession?.smtp_host
            if(allsession?.smtp_port && allsession?.smtp_port!="NA")
                port= allsession?.smtp_port

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.host",host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(frommail));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(sendto));

                if (cc_email) {
                    message.setRecipients(Message.RecipientType.CC,
                            InternetAddress.parse(cc_email))
                }
                if (bcc_email) {
                    message.setRecipients(Message.RecipientType.BCC,
                            InternetAddress.parse(bcc_email))
                }

                message.setSubject(subject);
                if (attachmentfile.equals("")) {
                    message.setContent(bodymessage, "text/html");
                }
                else {
                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();

                    // Now set the actual message
                    messageBodyPart.setContent(bodymessage, "text/html");

                    // Create a multipar message
                    Multipart multipart = new MimeMultipart();

                    // Set text message part
                    multipart.addBodyPart(messageBodyPart);

                    // Part two is attachment
                    messageBodyPart = new MimeBodyPart();
                    String filename = attachmentfile
                    DataSource source = new FileDataSource(filename);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(filename);
                    multipart.addBodyPart(messageBodyPart);
                    // Send the complete message parts
                    message.setContent(multipart);
                }
                Transport.send(message);
                System.out.println("Mail Sent Successfully.....");
                return 1
            }
            catch (MessagingException e) {
                // throw new RuntimeException(e);
                System.out.println("Error::Mail NOT Sent...." + e);
                return 0
            }
        }
    }



    def sesmail_withAWS(String from, String to, String body, String subject){
        if(to && to != 'director@vit.edu') {
            println "in sesmail msg:" + body

            final String FROM = from
            final String TO = to
            AWSCredential awsc = AWSCredential.findByNameAndIsactive("SES", true)
            final String BODY = body
            final String SUBJECT = subject

            final String SMTP_USERNAME = awsc.accesskey
            final String SMTP_PASSWORD = awsc.secretkey

            final String HOST = awsc.host

            final int PORT = awsc.port

            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", PORT);

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");

            Session session = Session.getDefaultInstance(props);

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
            msg.setSubject(SUBJECT);
            msg.setContent(BODY, "text/html");
            Transport transport = session.getTransport();
            try {
                transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
                transport.sendMessage(msg, msg.getAllRecipients());
                println("Email sent!");
            } catch (Exception ex) {
                System.out.println("The email was not sent.");
                System.out.println("Error message: " + ex.getMessage());
            } finally {
                transport.close();
            }
        }
    }



    //AWS TEMPLATE BASED API Begins



    def sendmailwithtemplate(String username, String password, String sendto, String subject, String template, def attachmentfile, def cc_email){
        println("sendmailwithtemplate")
        println("username "+username)
        println("sendto "+sendto)
        println("subject "+subject)
        println("password "+password)

        def allsession = getSession()
        def host="smtp.gmail.com"
        def port="587"
        println("allsession?.smtp_host "+allsession?.smtp_host)
        if(allsession?.smtp_host!="NA" && allsession?.smtp_host)
            host=allsession?.smtp_host
        if(allsession?.smtp_port!="NA" && allsession?.smtp_port)
            port= allsession?.smtp_port

        println("port "+port)
        println("host "+host)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

//        try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(sendto))
        if(cc_email) {
            message.setRecipients(Message.RecipientType.CC,
                    InternetAddress.parse(cc_email))
        }
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(template, "text/html");
        multipart.addBodyPart(messageBodyPart);

        if(attachmentfile?.size() > 0) {
            messageBodyPart = new MimeBodyPart();
            FileDataSource source = null;
            for(item in attachmentfile) {
                messageBodyPart = null;
                source = null;
                messageBodyPart = new MimeBodyPart();
                source = new FileDataSource(item?.file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(item?.name);
                multipart.addBodyPart(messageBodyPart);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
        println("Mail Sent Successfully.....");
        return true

//        } catch (MessagingException ex) {
//            println("Error::Mail NOT Sent...." + ex);
//        }
    }

    def admissionemailformat(def org, def email, def learner, def gsuitmsg){
        try {
            def sendemail = ERPRoleTypeSettings.findByNameIlikeAndOrganization('Send Email On Generate GR Number or Admission Confirmation', org)?.value
            if (sendemail == 'true') {
                if (org?.establishment_email && org?.establishment_email_credentials) {
    //                def msg = "Dear Student," +
    //                        "<br> Your Admission is Confirmed in " + org?.organization_name + " with PRN " + learner?.registration_number + "." +
    //                        "<br> Your New Login is created With Username " + learner?.uid + " and Password " + learner?.registration_number + "." +
    //                        "<br> You can login to ERP System using link " + org?.learner_website + "<br>" + gsuitmsg

    //                SendMailService.sendmailwithcss(org?.establishment_email, org.establishment_email_credentials, dal?.emailId, subject, msg, "", org?.establishment_email)

                    String subject = "Admission - Registration on ERP Portal"
                    def body = "<div style=\"width:90%; background:#fff;\">\n" +
                            "   <div class=\"body\" style=\"background:#f2edf5; padding:10px; \">\n" +
                            "      \n<div class=\"table-responsive\">\n" +
                            "      <table id=\"a\"  style=\"width:100%; font-size:16px;border: 1px solid black;border-collapse: collapse;\">\n" +
                            "         <tr>\n" +
                            "            <td style=\"width:15%;\">\n" +
                            "               <g:if test=\"${org?.org_logo}\">\n" +
                            "                  <center>\n" +
                            "                  <img  src=\"${org?.org_logo}\" style=\"height:100px !important; width:auto !important;\" />\n" +
                            "                  <center>\n" +
                            "               </g:if>\n" +
                            "            </td>\n" +
                            "            <td style=\"width:70%;\">\n" +
                            "               <center>\n" +
                            "                  <span style=\"font-size:16px; font-weight:900;\">${org?.organization_name}</span>\n" +
                            "               </center>\n" +
                            "            </td>\n" +
                            "            <td style=\"width:15%;\">\n" +
                            "               <g:if test=\"${org?.organizationgroup?.group_featured_logo && org?.organizationgroup?.group_featured_logo != null}\">\n" +
                            "                  <center>  <img class=\"float-center\" src=\"${org?.organizationgroup?.group_featured_logo}\" style=\"height:100px !important; width:auto !important;\" /></center>\n" +
                            "               </g:if>\n" +
                            "            </td>\n" +
                            "         </tr>\n" +
                            "      </table>\n" +
                            "   </div><br/>" +
                            "Dear " + learner?.person?.firstName?.trim()?.replaceAll(" +", " ") + " " + learner?.person?.middleName?.trim()?.replaceAll(" +", " ") + " " + learner?.person?.lastName?.trim()?.replaceAll(" +", " ") + "," +
                            "<p>Your Registration on ERP Portal is initiated in <b>" + org?.organization_name + " </b>with PRN <b>" + learner?.registration_number + "</b>.</p>" +
                            "<p>To kickstart your academic journey with us, we request you to complete your Registration on ERP.</p> " +
                            "<p>Please keep in mind that this process is mandatory and to be completed Immediately.</p>" +
                            "<p>We also request you to <b>upload Original Documents, Photo and Signature in the profile</b>.</p> " +
                            "<p>This step will enhance your academic experience by ensuring a smooth transition into your chosen course.</p>" +
                            "<br/>" +
                            "Please follow the link provided below to proceed with the Registration:\n" +
                            "<br/>" +
                            "<b>Web URL  : </b>" + org?.learner_website +
                            "<br/><b>Username :</b> " + learner?.uid +
                            "<br/><b>Password : </b>" + learner?.registration_number + "<br/>" + gsuitmsg +
                            "<br/><br/><b>Thanks & Regards,<br/>" +
                            "        " + org?.organization_name + "</b>" +
                            "    </p>" +
                            "      </p><br/><br/>" + "<center><img width='500px' src='https://vierp-test.s3.ap-south-1.amazonaws.com/epclogo/logo.png' /></center>" +
                            "   </div>" +
                            "</div>"

                    sendmailwithcss(org?.establishment_email, org.establishment_email_credentials, email, subject, body, "", org?.establishment_email)
                }
            }
        } catch (Exception e){
            return 0
        }
    }
    def getSession() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        return requestAttributes?.getRequest()?.getSession(false)
    }
    def msoffice()
    {
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        def session = getSession()
        println getSession()?.smtp_host
        println "session="+session?.smtp_host
        println "session="+session?.smtp_port
        return
        // SMTP server information
//        final String host = "lokmangalcolleges.org";
//        final String port = "587";
//        final String username = "noreply@lokmangalcolleges.org";
//        final String password = "lokmangal@2024@";

//        final String host = "smtp.office365.com";
//        final String port = "587";
//        final String username = "erp@bmionline.co.in";
//        final String password = "Bmssp@2024";
//
//        // Setup mail server properties
//        Properties properties = new Properties();
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true");
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port", port);
//
//        // Get the Session object
//        Session session = Session.getInstance(properties,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(username, password);
//                    }
//                });
//        try {
//            // Create a default MimeMessage object
//            Message message = new MimeMessage(session);
//
//            // Set From: header field
//            message.setFrom(new InternetAddress(username));
//
//            // Set To: header field
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("shivam@edupluscampus.com"));
//
//            // Set Subject: header field
//            message.setSubject("Test Email ");
//
//            // Set the actual message
//            message.setText("This is a test email sent ");
//
//            // Send the message
//            Transport.send(message);
//
//            System.out.println("Email sent successfully!");
//
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
    }
    def sendmailwithtemplateoffice(String username, String password, String sendto, String subject, String template, def attachmentfile, def cc_email){
        println("sendmailwithtemplateoffice")
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", "smtp.office365.com");
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

//        try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(sendto))
        if(cc_email) {
            message.setRecipients(Message.RecipientType.CC,
                    InternetAddress.parse(cc_email))
        }
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(template, "text/html");
        multipart.addBodyPart(messageBodyPart);

        if(attachmentfile?.size() > 0) {
            messageBodyPart = new MimeBodyPart();
            FileDataSource source = null;
            for(item in attachmentfile) {
                messageBodyPart = null;
                source = null;
                messageBodyPart = new MimeBodyPart();
                source = new FileDataSource(item?.file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(item?.name);
                multipart.addBodyPart(messageBodyPart);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
        println("Mail Sent Successfully.....");

//        } catch (MessagingException ex) {
//            println("Error::Mail NOT Sent...." + ex);
//        }
    }

}
