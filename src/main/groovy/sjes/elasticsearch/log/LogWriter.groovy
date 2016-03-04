package sjes.elasticsearch.log
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by 白 on 2016/3/4.
 */
class LogWriter {

    static Logger LOGGER = LoggerFactory.getLogger(LogWriter.class)
    static def logFile = new File("sjes-index-log")

    /**
     * 添加日志
     *
     * @param operate 操作 (backup, delete, index, restore)
     * @param status 状态(Start / Success|Fail)
     */
    static void append(String operate, String status) {
        def time = new Date().format('yyyy-MM-dd HH:mm:ss.SSS')
        logFile.append("[$time][$operate][$status]\n")
        LOGGER.debug("[$time][$operate][$status]")
    }
}
