package com.boxfox.cross.util;

import com.boxfox.cross.common.secure.AES256;
import com.boxfox.cross.common.secure.SHA256;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtil {

    public static File encryptFile(File file){
        try {
            String fileName = AES256.encrypt(file.getName());
            String str = AES256.encrypt(Files.toString(file, Charset.defaultCharset()));
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
            String str = AES256.decrypt(Files.toString(file, Charset.defaultCharset()));
            return str;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
