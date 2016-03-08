package sjes.elasticsearch.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by 白 on 2016/3/4.
 */
public class LogWriter {

    public static Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);
    public static String fileName = "sjes-index-log";
    public static FileWriter fileWritter;

    static {
        try {
            fileWritter = new FileWriter(fileName,true);
        } catch (IOException ignored) {}
    }

    public static void append(String operate, String status) {

        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            LOGGER.info("[" + time + "][" + operate + "][" + status + "]");
            fileWritter.append("[" + time + "][" + operate + "][" + status + "]\n");
            fileWritter.flush();
        } catch (IOException ignored) {}
    }
}
