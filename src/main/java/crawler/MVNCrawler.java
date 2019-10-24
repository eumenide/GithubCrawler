package crawler;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FileUtil;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * @author: eumes
 * @date: 2019/10/23
 **/
public class MVNCrawler {
    private static final Logger logger = Logger.getLogger(MVNCrawler.class.getName());

    private String basePath = "https://mvnrepository.com";
    private String popularPath = basePath + "/popular?p=";
    private static final MVNCrawler crawler = new MVNCrawler();

    private static MVNCrawler getCrawler() {
        return crawler;
    }

    private void run() {
        int threadSize = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        for (int i = 0; i < threadSize; i++) {
            final String url = popularPath + (i + 1);
            executorService.execute(new Runnable() {
                public void run() {
                    logger.info("start crawl form path=" + url);
                    try {
                        crawler.crawlering(url);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            });
        }
        executorService.shutdown();
    }

    private void crawlering(String url) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();
        Document document = Jsoup.connect(url).get();
        Elements elements = document.select("div.im > a");
        for (Element element1 : elements) {
            String jarProject = element1.attr("href");
            String urlOne = basePath + jarProject;
            logger.info("href to " + urlOne);

            Document document1 = Jsoup.connect(urlOne).get();
            Elements tHeads = document1.select("table.grid.versions > thead > tr > th");
            Elements elements1 = document1.select("table.grid.versions > tbody > tr");

            int versionIdx = 0;
            int useIdx = 0;
            for (int j = 0; j < tHeads.size(); j++) {
                if (tHeads.get(j).text().equals("Version")) {
                    versionIdx = j;
                } else if (tHeads.get(j).text().equals("Usages")) {
                    useIdx = j;
                }
            }

            int headSize = tHeads.size();
            int maxUse = 0;
            String maxVersion = "";
            int tmpUse;
            String jarUrl = "";

            for (Element element : elements1) {
                int tmpUseIdx = useIdx;
                int tmpVerIdx = versionIdx;
                Elements elements2 = element.select("td");
                int size = elements2.size();
                if (headSize > size) {
                    tmpUseIdx = useIdx - 1;
                    tmpVerIdx = versionIdx - 1;
                }
                if (elements2.get(tmpUseIdx).selectFirst("a") == null) {
                    tmpUse = 0;
                } else {
                    tmpUse = Integer.parseInt(elements2.get(tmpUseIdx).selectFirst("a").text().replaceAll(",", ""));
                }
                if (tmpUse > maxUse) {
                    maxUse = tmpUse;
                    maxVersion = elements2.get(tmpVerIdx).selectFirst("a").text();
                    jarUrl = elements2.get(tmpVerIdx).selectFirst("a").attr("href");
                }
            }
            logger.info(urlOne + " maxUse=" + maxUse + " version=" + maxVersion);

            // 进入使用最多的版本主页
            String jarName = jarUrl.substring(0, jarUrl.lastIndexOf("/")) + "-" + maxVersion + ".jar";
            jarUrl = basePath + jarProject.substring(0, jarProject.lastIndexOf("/") + 1) + jarUrl;

            Document document2 = Jsoup.connect(jarUrl).get();
            Element element = document2.selectFirst("div#maincontent > table.grid > tbody > tr > td > a:matchesOwn(View All)");
            if (element == null) {
                logger.error("cannot found View All in url : " + jarUrl);
                continue;
            }
            String loadUrl = element.attr("href") + "/" + jarName;

            logger.info("download from " + loadUrl);
            String result = FileUtil.downloadFile(loadUrl, jarName);
            if ("error".equals(result)) {
                logger.error("download error");
            } else {
                logger.info("download end: " + result);
            }
        }

        sw.stop();
        logger.info("共耗时： " + sw.getTime(TimeUnit.SECONDS) + "s");
    }


    public static void main(String[] args) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        MVNCrawler crawler = MVNCrawler.getCrawler();
        crawler.run();

        sw.stop();
        logger.info("共耗时： " + sw.getTime(TimeUnit.SECONDS) + "s");
    }
}
