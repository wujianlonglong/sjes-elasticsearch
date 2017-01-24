package sjes.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.service.BackupService;
import sjes.elasticsearch.service.SearchLogService;
import sjes.elasticsearch.service.SearchService;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by 白 on 2016/2/25.
 */
@Component
public class ScheduledTasks {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchLogService searchLogService;

    @Autowired
    private BackupService backupService;

    @Value("${elasticsearch-backup.retry.backup}")
    private int backupFailRetryTimes;       //备份失败重试次数

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    //每天凌晨三点自动更新索引
    @Scheduled(cron="0 0 3 * * *")
    public void autoIndex() throws ServiceException, IOException {

        int retryTimes = backupFailRetryTimes;
        boolean isBackupSucceed;

        do {
            isBackupSucceed = backupService.backup();
        }while (!isBackupSucceed && retryTimes-- > 0);

        searchService.deleteIndex();
        searchService.initService();
    }
}
