package utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: eumes
 * @date: 2019/10/23
 **/
public class FileUtil {

//    private static String destPath = "D:\\tmp\\mvn\\";
    private static String destPath = "/home/eumes/project/mvnrepo/";

    public static String downloadFile2(String url, String fileName) {
        String[] cmds = {"cmd.exe", "/c", "curl", url, "-o", destPath+fileName};
        String cmd = "cmd.exe /c curl " + url + " -o " + destPath + fileName;
        ProcessBuilder process = new ProcessBuilder(cmds);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }

            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error";
    }

    public static String downloadFile(String url, String fileName) {
        try {
            URL httpurl = new URL(url);
            File dirFile = new File(destPath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            FileUtils.copyURLToFile(httpurl, new File(destPath + fileName));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "error";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        return "success";
    }

    public static void main(String[] args) {
        System.out.println(downloadFile("", ""));
    }
}
