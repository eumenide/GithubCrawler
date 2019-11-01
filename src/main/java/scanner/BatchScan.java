package scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import utils.FileUtil;
import utils.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: eumes
 * @date: 2019/10/29
 **/
public class BatchScan {
    private final static Logger logger = Logger.getLogger(BatchScan.class);

    private final static String baseDir = "/home/eumes/project/";
    private final static String fileDir = "/home/eumes/project/mvnrepo/";
    private final static String resDir = "/home/eumes/project/results/";

    private final static int threadSize = 4;

    private static void batchScan() {
        File jarDir = new File(fileDir);
        File[] jarFiles = jarDir.listFiles();
        List<String> jarFilePathList = new ArrayList<>();
        for (File file : Objects.requireNonNull(jarFiles)) {
            jarFilePathList.add(file.getAbsolutePath());
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        int gap = jarFilePathList.size() / threadSize;

        logger.info("start scan jar files, threadSize=" + threadSize + ", totalFile=" + jarFilePathList.size());

        for (int i = 0; i < threadSize; i++) {
            int startIdx = i * gap;
            int endIdx = (i + 1) * gap;

            if (i == threadSize - 1) {
                endIdx = jarFilePathList.size();
            }

            final List<String> singleList = jarFilePathList.subList(startIdx, endIdx);

            Runnable run = () -> singleScan(singleList);
            executorService.execute(run);
        }
    }

    private static void singleScan(List<String> filePaths) {
        for (String filePath : filePaths) {
            File jarFile = new File(filePath);
            String fileName = jarFile.getName().substring(0, jarFile.getName().lastIndexOf(".jar"));
            String curWorkPath = resDir + fileName;
            String resName = curWorkPath + "/result.json";
            String errName = curWorkPath + "/error.log";
            File resFile = new File(resName);
            File errFile = new File(errName);
            File curWorkSpace = new File(curWorkPath);
            if (!curWorkSpace.exists()) {
                curWorkSpace.mkdir();
            }
            try {
                FileUtils.copyFileToDirectory(jarFile, curWorkSpace);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            FileUtil.findFiles(".class", );
            logger.info("start to count " + fileName + ".jar" );
            int count = 0;
            try {
                count = FileUtil.countJarClassFiles(filePath);
            } catch (IOException e) {
                logger.info("count " + fileName + ".jar error: " + e.getMessage());
            }

            logger.info("start to scan " + fileName + ".jar fileSize=" + jarFile.length() / 1000 + "KB");
            StopWatch sw = new StopWatch();
            sw.start();
            String cmd = "docker run --rm -v " + curWorkPath + "/:/workspace mt_jsc:1.1 start -F /workspace/" + jarFile.getName();
            try {
                Process process = Runtime.getRuntime().exec(cmd);
                List<String> outputList = ProcessUtil.processMessageToString(process.getInputStream());
                List<String> errorList = ProcessUtil.processMessageToString(process.getErrorStream());

                FileUtils.writeLines(resFile, outputList);
                FileUtils.writeLines(errFile, errorList);
                process.waitFor(60, TimeUnit.MINUTES);
            } catch (IOException | InterruptedException e) {
                logger.error("scan " + fileName + ".jar : " + e.getMessage());
            }

            sw.stop();
            logger.info("scan " + fileName + ".jar end : time=" + sw.toString() + " total=" + count);
        }
    }

    public static void main(String[] args) {
        batchScan();
    }
}
