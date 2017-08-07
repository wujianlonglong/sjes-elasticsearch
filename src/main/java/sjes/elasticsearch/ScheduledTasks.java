package sjes.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.service.*;
import sjes.elasticsearch.serviceaxsh.BackupAxshService;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;

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
    private SearchAxshService searchAxshService;


    @Autowired
    private SearchLogService searchLogService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private BackupAxshService backupAxshService;

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



    //每天凌晨三点十五分自动更新索引Axsh
    @Scheduled(cron="0 15 3 * * *")
    public void autoIndexAxsh() throws ServiceException, IOException {

        int retryTimes = backupFailRetryTimes;
        boolean isBackupSucceed;

        do {
            isBackupSucceed = backupAxshService.backup();
        }while (!isBackupSucceed && retryTimes-- > 0);

        searchAxshService.deleteIndex();
        searchAxshService.initService();
    }
}
