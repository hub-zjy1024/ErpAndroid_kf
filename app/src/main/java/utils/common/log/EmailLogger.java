package utils.common.log;

import android.content.Context;

import java.util.Date;

import utils.common.EmailSender;
import utils.common.UploadUtils;

/**
 * Created by 张建宇 on 2019/6/24.
 */
public class EmailLogger {
    public static void sendLogSync(final String logMessage, final Context mContext) {
        Runnable sendRun = new Runnable() {
            @Override
            public void run() {
                sendLog(logMessage, mContext);
            }
        };
        Thread m = new Thread(sendRun);
        m.start();
    }
    public static void sendLog(String logMessage, Context mContext) {
        EmailSender sender = new EmailSender();
        String devMessage = UploadUtils.getPhoneCode(mContext);
        String time = "sendTime=" + new Date().toLocaleString() ;
        String encryptedMsg = sender.encrypte(logMessage);
        String finalMsg = devMessage + "\n";
        finalMsg+= time + "\n" ;
        finalMsg+= encryptedMsg + "\n" ;
        sender.sendCommonMail(finalMsg);
    }
}
