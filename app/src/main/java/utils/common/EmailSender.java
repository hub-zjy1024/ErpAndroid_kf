package utils.common;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by 张建宇 on 2019/5/28.
 * <br>
 * java依赖
 *  <br>
 * //    implementation 'com.sun.mail:javax.mail:1.6.2'
 //    implementation   'javax.activation:activation:1.1.1'
 <br>
 android依赖
 <br>
 implementation 'com.sun.mail:android-mail:1.5.6'
 implementation 'com.sun.mail:android-activation:1.5.6'
 */
public class EmailSender {
    private String fromTag = "dyjKF";
    private String mailTitle = fromTag + "日志";

    private final String myEmailAccount = "xx0012301@163.com";
    private final String myEmailPassword = "love840502";
    private final String cs = "utf-8";
    /*  private final String myEmailAccount = "17767177737@qq.com";
      private final String myEmailPassword = "chhbfeoenntwdbhb";
    *//*  private final String myEmailPassword = "17767177737abcde";
     */
    // 发件人邮箱的 SMTP 服务器地址, 必须准确, 不同邮件服务器地址不同, 一般(只是一般, 绝非绝对)格式为: smtp.xxx.com
    // 网易163邮箱的 SMTP 服务器地址为: smtp.163.com
    public String myEmailSMTPHost = "smtp.163.com";
    // 收件人邮箱（替换为自己知道的有效邮箱）
    public String receiveMailAccount = "851280963@qq.com";

    public EmailSender() {
    }

    Properties intProperties(){
        Properties props = new Properties();    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");  // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", myEmailSMTPHost);
        props.setProperty("mail.smtp.port", "25");//设置端口 // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");  // 需要请求认证
        return props;
    }

    String encrypte(String msg) {
        return Base64.encodeToString(msg.getBytes(), Base64.NO_WRAP);
    }

    public void sendMail(String msg) {
        Properties props = intProperties();
        Session session = Session.getInstance(props,  new Authenticator() {
            // 设置认证账户信息
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmailAccount, myEmailPassword);
            }
        });
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream psDebugout = new PrintStream(bao);
        session.setDebugOut(psDebugout);
        session.setDebug(true);

        String time = "sendTime=" + new Date().toLocaleString() + "\n";
        msg = time + msg;
        String real = encrypte(msg);
        try {
            MimeMessage  message = createMimeMessage(session, real);
            Transport.send(message, myEmailAccount, myEmailPassword);
            psDebugout.flush();
            String logInfo = new String(bao.toByteArray());
            Log.e("zjy", getClass() + "->sendMail(): ==debugMsg=" + logInfo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            psDebugout.close();
        }
    }

    public String sendMail() {
        Properties props = intProperties();
        // props.setProperty("mail.smtp.starttls.enable", "true");  // 需要请求认证
        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props, new Authenticator() {
            // 设置认证账户信息
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmailAccount, myEmailPassword);
            }
        });
//        session.setDebug(true);       // 设置为debug模式, 可以查看详细的发送 log
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        PrintStream m = new PrintStream(bao);
//        session.setDebugOut(m);

        Transport transport = null;
        try {
            // 3. 创建一封邮件
            MimeMessage message =createMimeMessage(session, myEmailAccount, receiveMailAccount);
//            transport = session.getTransport();
//            transport.connect(myEmailAccount, myEmailPassword);
////            transport.connect();
//            // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
//            transport.sendMessage(message, message.getAllRecipients());
//            // 7. 关闭连接
//            transport.close();
            Transport.send(message, myEmailAccount, myEmailPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            bao.flush();
            //        Log.e("zjy", getClass() + "->sendMail(): debugMsg==" + new String(bao.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bao.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

     MimeMessage createMimeMessage(Session session, String msg) throws
            Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(myEmailAccount, fromTag, cs));
        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress[]{
                new InternetAddress(receiveMailAccount, "qq用户", cs)
        });
        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject(mailTitle, cs);
        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
         message.setContent(msg, "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());
        // 7. 保存设置
        message.saveChanges();
        return message;
    }

     MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail) throws
            Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(sendMail, fromTag,cs));
        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress[]{
                new InternetAddress(receiveMail, "qq用户", cs)
        });
        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject(mailTitle, cs);
        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        message.setContent("hello word test mail", "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());
        // 7. 保存设置
        message.saveChanges();
        return message;
    }

    Multipart getSimpleContent(String msg) throws MessagingException, UnsupportedEncodingException {
        //正文
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(msg,"text/html;charset=UTF-8");
        //图片
//        MimeBodyPart image = new MimeBodyPart();
//        image.setDataHandler(new DataHandler(new FileDataSource("src\\3.jpg")));
//        image.setContentID("aaa.jpg");
//        //附件1
//        MimeBodyPart attach = new MimeBodyPart();
//        DataHandler dh = new DataHandler(new FileDataSource("src\\4.zip"));
//        attach.setDataHandler(dh);
//        attach.setFileName(dh.getName());

        //附件2
//        MimeBodyPart attach2 = new MimeBodyPart();
//        DataHandler dh2 = new DataHandler(new FileDataSource("src\\波子.zip"));
//        attach2.setDataHandler(dh2);
//        attach2.setFileName(MimeUtility.encodeText(dh2.getName()));

        //描述关系:正文和图片
        MimeMultipart mp1 = new MimeMultipart();
        mp1.addBodyPart(text);
//        mp1.addBodyPart(image);
        mp1.setSubType("related");

        //描述关系:正文和附件
        MimeMultipart mp2 = new MimeMultipart();
//        mp2.addBodyPart(attach);
//        mp2.addBodyPart(attach2);

        //代表正文的bodypart
        MimeBodyPart content = new MimeBodyPart();
        content.setContent(mp1);

        mp2.addBodyPart(content);
        mp2.setSubType("mixed");
        return mp2;
    }

    //    public boolean send(Mail mail) {
    //        // 发送email
    //        HtmlEmail email = new HtmlEmail();
    //        try {
    //
    //            // 这里是SMTP发送服务器的名字：163的如下："smtp.163.com"，qq的：smtp.qq.com
    //            email.setHostName(mail.getHost());
    //
    //            // 字符编码集的设置
    //            email.setCharset(Mail.ENCODEING);
    //            // 收件人的邮箱
    //            email.addTo(mail.getReceiver());
    //            // 发送人的邮箱
    //            email.setFrom(mail.getSender(), mail.getName());
    //            // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码
    //            email.setAuthentication(mail.getUsername(), mail.getPassword());
    //            // 要发送的邮件主题
    //            email.setSubject(mail.getSubject());
    //            // 要发送的信息，由于使用了HtmlEmail，可以在邮件内容中使用HTML标签
    //            email.setMsg(mail.getMessage());
    //            // 发送
    //            email.send();
    //
    //            return true;
    //        } catch (EmailException e) {
    //            e.printStackTrace();
    //
    //            return false;
    //        }
    //    }
}
