package utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUpLoad {

    public static boolean upload(String requestUrl, File file) {
        if (file == null) {
            return false;
        }
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(1000);
            conn.setUseCaches(false);
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            fis.close();
            out.flush();
            Bitmap bitmap;
            Base64.encodeToString(buf, Base64.DEFAULT);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
