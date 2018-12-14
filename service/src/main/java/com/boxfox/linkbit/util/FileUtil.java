package com.boxfox.linkbit.util;

import com.boxfox.vertx.secure.AES256;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtil {

    public static File encryptFile(File file){
        try {
            String fileName = AES256Util.encrypt(file.getName());
            String str = AES256Util.encrypt(Files.toString(file, Charset.defaultCharset()));
            File newFile = new File(file.getPath()+"/"+fileName);
            Files.write(str, newFile, Charset.defaultCharset());
            file.delete();
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptFile(File file){
        try {
            String str = AES256Util.decrypt(Files.toString(file, Charset.defaultCharset()));
            return str;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
