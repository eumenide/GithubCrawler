package utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    /**
     * 寻找指定目录下，具有指定后缀名的所有文件。
     *
     * @param filenameSuffix : 文件后缀名
     * @param currentDirUsed : 当前使用的文件目录
     * @param currentFilenameList ：当前文件名称的列表
     */
    public static void findFiles(String filenameSuffix, String currentDirUsed,
                          List<String> currentFilenameList) {
        File dir = new File(currentDirUsed);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                /**
                 * 如果目录则递归继续遍历
                 */
                findFiles(filenameSuffix,file.getAbsolutePath(), currentFilenameList);
            } else {
                /**
                 * 如果不是目录。
                 * 那么判断文件后缀名是否符合。
                 */
                if (file.getAbsolutePath().endsWith(filenameSuffix)) {
                    currentFilenameList.add(file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 获取jar包目录下所有的class文件数量
     *
     * @param jarPath jar包的路径+名称
     * @return 返回对应jar包所有.class文件的数量
     */
    public static int countJarClassFiles(String jarPath) throws IOException {
        int count = 0;
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(countJarClassFiles("D:\\tmp\\jbpm-flow-7.28.0.Final.jar"));
//        System.out.println(downloadFile("", ""));
    }
}
