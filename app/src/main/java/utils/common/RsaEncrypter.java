package utils.common;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class RsaEncrypter {
    //	private static Logger mLogger = LoggerFactory.getLogger(RsaEncrypter.class);
    //		private static String publicKey = "";
    //		private static String privateKey = ""
    protected String publicKey =
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBA6Rl2zhuBtQSEvkVpqWQwTnhwe2uGUSpIufFP2yLH0sbMPAjTkycQk9XGQnwsnjJ3V/ptbjGom2ILTd7hHJMtb9hhfcFRcOIKTMaMO3XD9nYI+R3xO2jhMu2bPVBLKTarKvmfklr8pvNjvetOaCbng3rwkkjcjv68kLZlhy3YQIDAQAB";
    protected String privateKey =
			"MIICXAIBAAKBgQDBA6Rl2zhuBtQSEvkVpqWQwTnhwe2uGUSpIufFP2yLH0sbMPAjTkycQk9XGQnwsnjJ3V" +
					"/ptbjGom2ILTd7hHJMtb9hhfcFRcOIKTMaMO3XD9nYI" +
					"+R3xO2jhMu2bPVBLKTarKvmfklr8pvNjvetOaCbng3rwkkjcjv68kLZlhy3YQIDAQABAoGAB6QfxYKEvOJbUe3bW46R3mWv526YfLx2WeXOXCIzJ1zRSd3Jm/Q1FziO0Ilmudcu7frsGaH+kyqKAIqduC+ZoLsQgeT4cAotzNGGZRn0fANsE6fxEgxt7AcWCODnIWbEUsOHbeVFyjF/7SadVhO/+dJnmX/LGM/yw2RS/3QjanMCQQD8sJIAWk0KE7R17ZcRkaTrejf2lWuWVs3S4KykTrrLivpfPglDkVrS8PwR0DXli55c9TXtFLMbWgH//D1IXN4LAkEAw4rxHw+ew8TYDxzMWcO0P3+8MpC2ryjSVuN+Uc0f0lo0x13Wc1Zi/c/BZs5+94YoMAjI1mEgQ5XQlOUyElzfwwJBAKWXBCZtBp066oB5UQ03V07kybWyl01u1vSBPUFzQl/OVGKDociAgXdIarc1rYweYYnjOxKBBRpAcp0Q7Av2p58CQBqWdsihaBX4WuRbJxIBgS2tIZrCgIR6iXcVAaT/vhbs+wYspS8TjOwz5nkjFLJ1RFubpis4E5n88dp8+3zxsd8CQFFdPrYtaR8NaK/iPHk2NcvAdS5FPtOUu0FZJDy8X32agfADgFoaAp4r/6NPU2KQt6OvT8go9q//t3DI+U6NxdY=";
    //	protected static String chiperMode = "RSA/None/NoPadding";
    protected static String chiperMode = "RSA";
    //	public static int key_bit = 2048;
    protected int key_bit = 1024;
    protected int key_len = key_bit / 8;

    public RsaEncrypter(String publicKey, String privateKey, int key_bit) {
        super();
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.key_bit = key_bit;
        this.key_len = this.key_bit / 8;
        init();
    }

    RSAPrivateKey mPvKey = null;
    RSAPublicKey mPubKey = null;

    public RsaEncrypter() {
        init();
    }

    private void init() {
        byte[] decoded = base64DecodeToByte(privateKey);
        byte[] decoded2 = base64DecodeToByte(publicKey);
        KeySpec kspec = new PKCS8EncodedKeySpec(decoded);
        KeySpec pub2 = new X509EncodedKeySpec(decoded2);
        try {
            KeyFactory instance = KeyFactory.getInstance("RSA");
            mPvKey = (RSAPrivateKey) instance.generatePrivate(kspec);
            Log.e("zjy", getClass() + "->init():KeyFactory provider1==" + instance.getProvider().toString());
            mPubKey = (RSAPublicKey) instance.generatePublic(pub2);
            Log.e("zjy", getClass() + "->init():KeyFactory provider2 ==" +instance.getProvider().toString());
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String mMsg = "62105300";
        //		String mMsg =
		//		"62105300
		//		发送的看来附近拉撒的减肥了阿斯加德了放空精神了困就山东矿机拉萨大街拉伸拉伸爱神的箭发四六级拉伸宽带缴费拉丝机的发大立科技死啦肯德基发送垃圾袋拉伸控件的拉丝机卡三等奖速度快垃圾拉丝机拉烧烤就阿萨德阿斯蒂芬洒阿萨德是啊"
        //				+
		//				"62105300
		//				发送的看来附近拉撒的减肥了阿斯加德了放空精神了困就山东矿机拉萨大街拉伸拉伸爱神的箭发四六级拉伸宽带缴费拉丝机的发大立科技死啦肯德基发送垃圾袋拉伸控件的拉丝机卡三等奖速度快垃圾拉丝机拉烧烤就阿萨德阿斯蒂芬洒阿萨德是啊"
        //				+
		//				"62105300
		//				发送的看来附近拉撒的减肥了阿斯加德了放空精神了困就山东矿机拉萨大街拉伸拉伸爱神的箭发四六级拉伸宽带缴费拉丝机的发大立科技死啦肯德基发送垃圾袋拉伸控件的拉丝机卡三等奖速度快垃圾拉丝机拉烧烤就阿萨德阿斯蒂芬洒阿萨德是啊"
        //				+
		//				"62105300
		//				发送的看来附近拉撒的减肥了阿斯加德了放空精神了困就山东矿机拉萨大街拉伸拉伸爱神的箭发四六级拉伸宽带缴费拉丝机的发大立科技死啦肯德基发送垃圾袋拉伸控件的拉丝机卡三等奖速度快垃圾拉丝机拉烧烤就阿萨德阿斯蒂芬洒阿萨德是啊"
        //				+
		//				"62105300
		//				发送的看来附近拉撒的减肥了阿斯加德了放空精神了困就山东矿机拉萨大街拉伸拉伸爱神的箭发四六级拉伸宽带缴费拉丝机的发大立科技死啦肯德基发送垃圾袋拉伸控件的拉丝机卡三等奖速度快垃圾拉丝机拉烧烤就阿萨德阿斯蒂芬洒阿萨德是啊";
        RsaEncrypter rasE = new RsaEncrypter();
        try {
            //			String resp = rasE.encrypt(mMsg);
            //			String deSrc = rasE.decrypt(resp);
            String resp = rasE.encrypt(mMsg);
            String deSrc = rasE.decrypt(resp);
            //			mLogger.info("isMatch={}", deSrc.equals(mMsg));
            //			mLogger.info("resp={}\ndeSrc={}", resp, deSrc);
            //			mLogger.info(String.format("src=%s,\nrsaRes =%s，\ndeSrc=%s", mMsg, deSrc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        RsaEncrypter encrypter = new RsaEncrypter();
        String mdata = "这是一个测试123123";
        try {
            String encrypt = encrypter.encrypt(mdata);
            Log.e("zjy", RsaEncrypter.class.getClass() + "->test(): ==encrypt" + encrypt);
            String decrypt = encrypter.decrypt(encrypt);
            Log.e("zjy", RsaEncrypter.class.getClass() + "->test(): ==decrypt" + decrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decrypt(String msg)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(chiperMode);
        Provider provider = cipher.getProvider();
        Log.e("zjy", getClass() + "->decrypt():cipher provide ==" + provider);
        cipher.init(Cipher.DECRYPT_MODE, mPvKey);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] bytes = base64DecodeToByte(msg);
        int dataLen = bytes.length;
        int maxLength = key_len;
        int i = 0;
        while (i < dataLen) {
            int len = maxLength;
            if (i + maxLength >= dataLen) {
                len = dataLen - i;
            }
            byte[] tBytes = new byte[len];
            System.arraycopy(bytes, i, tBytes, 0, len);
            byte[] newBytes = cipher.doFinal(tBytes);
            try {
                bao.write(newBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            i += maxLength;
        }
        final String data = new String(bao.toByteArray());
        return data;
    }

    public String encrypt(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        //RSA加密
        Cipher cipher = Cipher.getInstance(chiperMode);
        cipher.init(Cipher.ENCRYPT_MODE, mPubKey);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] bytes = msg.getBytes();
        int dataLen = bytes.length;
        int maxLength = key_len - 11;
        int i = 0;
        while (i < dataLen) {
            int len = maxLength;
            if (i + maxLength >= dataLen) {
                len = dataLen - i;
            }
            byte[] tBytes = new byte[len];
            System.arraycopy(bytes, i, tBytes, 0, len);
            byte[] newBytes = cipher.doFinal(tBytes);
            try {
                bao.write(newBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            i += maxLength;
        }
        String outStr = base64Encode(bao.toByteArray());
        return outStr;
    }

    private static String base64Encode(byte[] data) {
        //		String base64 = new String(Base64.getEncoder().encode(data));
        //		Base64.encodeToString(data, Base64.NO_WRAP);
        //		return base64;
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    //	private static String base64Decode(String bytes) {
    //		byte[] msg = Base64.getDecoder().decode(bytes.getBytes());
    //		return new String(msg);
    //	}

    private static byte[] base64DecodeToByte(String bytes) {
        //		byte[] msg = Base64.getDecoder().decode(bytes.getBytes());
        //		return msg;
        return Base64.decode(bytes, Base64.NO_WRAP);
    }

    //	private static byte[] base64EncodeToByte(String bytes) {
    //		byte[] msg = Base64.getEncoder().encode(bytes.getBytes());
    //		return msg;
    //	}

    private static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = base64DecodeToByte(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    private static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = base64DecodeToByte(key);
        KeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey publicKey = keyFactory.generatePrivate(keySpec);
        return publicKey;
    }
}
