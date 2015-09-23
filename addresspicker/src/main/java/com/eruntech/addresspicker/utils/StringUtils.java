package com.eruntech.addresspicker.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <P>作者：Qin Yuanyi
 * <P>时间：2015-09-23
 * <P>功能：关于String类的一些工具方法
 */
public class StringUtils {

    /**
     * <P>修改时间：2015-09-23
     * <P>作者：Qin Yuanyi
     * <P>功能描述：将InputStream使用UTF-8编码转化为最大长度为1024的String
     * @param inputStream 需要转化为String型的InputStream
     */
    public static String InputStreamToString(InputStream inputStream) throws IOException {
        return InputStreamToString(inputStream, 1024, "UTF-8");
    }
    /**
     * <P>修改时间：2015-09-23
     * <P>作者：Qin Yuanyi
     * <P>功能描述：将InputStream转化为最大长度为1024的String
     * @param inputStream 需要转化为String型的InputStream
     * @param encoding 编码
     */
    public static String InputStreamToString(InputStream inputStream, String encoding) throws IOException {
        return InputStreamToString(inputStream, 1024, encoding);
    }
    /**
     * <P>修改时间：2015-09-23
     * <P>作者：Qin Yuanyi
     * <P>功能描述：将InputStream转化为String
     * @param inputStream 需要转化为String型的InputStream
     * @param maxStringLength 转化后的字符串的最大长度
     * @param encoding 编码
     */
    public static String InputStreamToString(InputStream inputStream, int maxStringLength, String encoding) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[maxStringLength];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return new String(baos.toByteArray(), encoding);
    }
}
