package com.modou.bledemo.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertUtils {

    /**
     * ����ģʽ
     */
    static ConvertUtils instance = null;// ���

    private ConvertUtils() {
    }

    public static ConvertUtils getInstance() {
        if (instance == null)
            instance = new ConvertUtils();
        return instance;
    }

    /**
     * byte����ת��Ϊʮ�������ַ���
     *
     * @param b
     * @return
     */
    public String bytesToHexString(byte[] b) {
        if (b.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < b.length; i++) {
            int value = b[i] & 0xFF; // ��λ�����㣬����1ʱ���Ϊ1������Ϊ0��������ȫ��1���룬�������ԭ����01��
            String hv = Integer.toHexString(value);
            if (hv.length() < 2) {
                sb.append(0);
            }

            sb.append(hv);
        }
        return sb.toString();
    }

    public static String getTime(){
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");//���Է�����޸����ڸ�ʽ
        String time = dateFormat.format(now);
        return time;
    }

}
