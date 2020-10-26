package com.rengu.operationsmanagementsuitev3.Utils;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 13:08
 **/

@Slf4j
public class FormatUtils {

    /**
     * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的"/"符号。
     *
     * @param path 文件路径
     * @return 格式化后的文件路径
     */
    public static String formatPath(String path) {
        String reg0 = "\\\\＋";
        String reg = "\\\\＋|/＋";
        String temp = path.trim().replaceAll(reg0, "/");
        temp = temp.replaceAll(reg, "/");
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        if (System.getProperty("file.separator").equals("\\")) {
            temp = temp.replace('/', '\\');
        }
        return FilenameUtils.separatorsToUnix(temp);
    }

    // 生成指定长度的字符串
    public static String getString(String string, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(string);
        stringBuilder.setLength(length);
        return stringBuilder.toString();
    }

    // 从字符串生成指定长度的字节数组
    public static byte[] getBytesFormString(String string, int length) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(string.getBytes());
        return byteBuffer.array();
    }


    // 递归拼接path信息
    public static String getComponentFileRelativePath(ComponentFileEntity componentFileEntity, String basePath) {
        if (basePath.isEmpty()) {
            if (componentFileEntity.isFolder()) {
                basePath = File.separatorChar + componentFileEntity.getName() + File.separatorChar;
                System.out.println("路径1：" + basePath);
            } else {
                basePath = StringUtils.isEmpty(componentFileEntity.getFileEntity().getType()) ? File.separatorChar + componentFileEntity.getName() : File.separatorChar + componentFileEntity.getName() + "." + componentFileEntity.getFileEntity().getType();
                System.out.println("路径2：" + basePath);
            }
        }
        while (componentFileEntity.getParentNode() != null) {
            componentFileEntity = componentFileEntity.getParentNode();
            basePath = File.separatorChar + componentFileEntity.getName() + basePath;
            getComponentFileRelativePath(componentFileEntity, basePath);
        }
        return FormatUtils.formatPath(basePath);
    }

    // 递归拼接path信息
    public static String getComponentFileRelativePaths(ComponentFileEntity componentFileEntity, String basePath) {
        if (basePath.isEmpty()) {
            if (componentFileEntity.isFolder()) {
                basePath = File.separator + componentFileEntity.getName() + File.separator;
                System.out.println("路径1：" + basePath);

            } else {
                basePath = StringUtils.isEmpty(componentFileEntity.getFileEntity().getType()) ? File.separator + componentFileEntity.getName() : File.separator + componentFileEntity.getName() + "." + componentFileEntity.getFileEntity().getType();
                System.out.println("路径2：" + basePath);
            }
        }
        while (componentFileEntity.getParentNode() != null) {
            componentFileEntity = componentFileEntity.getParentNode();
            basePath = File.separator + componentFileEntity.getName() + basePath;
            getComponentFileRelativePaths(componentFileEntity, basePath);
        }
        String basePaths = formatPath(basePath);
        return FormatUtils.formatPath(basePaths);
    }

    // 递归拼接path信息
    public static String getComponentFileHistoryRelativePath(ComponentFileHistoryEntity componentFileHistoryEntity, String basePath) {
        if (basePath.isEmpty()) {
            if (componentFileHistoryEntity.isFolder()) {
                basePath = File.separatorChar + componentFileHistoryEntity.getName() + File.separatorChar;
            } else {
                basePath = StringUtils.isEmpty(componentFileHistoryEntity.getFileEntity().getType()) ? File.separatorChar + componentFileHistoryEntity.getName() : File.separatorChar + componentFileHistoryEntity.getName() + "." + componentFileHistoryEntity.getFileEntity().getType();
            }
        }
        while (componentFileHistoryEntity.getParentNode() != null) {
            componentFileHistoryEntity = componentFileHistoryEntity.getParentNode();
            basePath = File.separatorChar + componentFileHistoryEntity.getName() + basePath;
            getComponentFileHistoryRelativePath(componentFileHistoryEntity, basePath);
        }
        return FormatUtils.formatPath(basePath);
    }

    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static int filterChinese(String str) {
        // 用于返回结果

        boolean flag = isContainChinese(str);

        int zwNum = 0;
        if (flag) {// 包含中文
            // 用于拼接过滤中文后的字符
            StringBuffer sb = new StringBuffer();
            // 用于校验是否为中文
            boolean flag2 = false;
            // 用于临时存储单字符
            char chinese = 0;
            // 5.去除掉文件名中的中文
            // 将字符串转换成char[]
            char[] charArray = str.toCharArray();
            // 过滤到中文及中文字符
            for (int i = 0; i < charArray.length; i++) {
                chinese = charArray[i];
                flag2 = isChinese(chinese);
                if (flag2) {// 不是中日韩文字及标点符号
                    zwNum++;
                }
            }

        }
        return zwNum;
    }

    /**
     * 判断字符串中是否包含中文
     *
     * @param str 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 判定输入的是否是汉字
     *
     * @param c 被校验的字符
     * @return true代表是汉字
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }


    //获取字符串的字节长度
    public static int getLength(String s) {
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255) {
                length++;
            } else {
                length += 2;
            }
        }
        return length;
    }

    //将int类型转为二进制
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
//        result[0] = (byte) ((i >> 24) & 0xFF);
//        result[1] = (byte) ((i >> 16) & 0xFF);
//        result[2] = (byte) ((i >> 8) & 0xFF);
//        result[3] = (byte) (i & 0xFF);
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    //把字节数组转为int
    public static int byteArrToInt(byte[] arr){
        System.out.print("需要转换的字节数组是:");
        for(byte temp : arr){
            System.out.print(temp+" ");
        }
        System.out.println("");
        int x = ((arr[0] & 0xff) << 0 )|((arr[1]& 0xff) <<8 )|((arr[2] & 0xff)<<16)|((arr[3] & 0xff)<<24);
        System.out.println("转换后的整数是:"+x);
        return x;
    }

    //中文字符串改二进制
    public static String StrToBinstr(String str) {
        char[] strChar = str.toCharArray();
        String result = "";
        for (int i = 0; i < strChar.length; i++) {
            result += Integer.toBinaryString(strChar[i] & 0xff);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(filterChinese("ajhajbjabsjkaj你是死123131"));
        System.out.println(new String("ajhajbjabsjkaj123131").getBytes().length);
        System.out.println(new String("ajhajbjabsjkaj你是死123131").getBytes().length);
    }
}
