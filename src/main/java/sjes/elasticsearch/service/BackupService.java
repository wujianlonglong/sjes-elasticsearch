package sjes.elasticsearch.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.utils.ElasticsearchSnapshotUtils;

import java.io.IOException;

/**
 * Created by 白 on 2016/1/7.
 */
@Service("backupService")
public class BackupService {

    @Value("${elasticsearch-backup.url}")
    private String elasticsearchUrl;        //elasticsearch 地址

    @Value("${elasticsearch-backup.repository.name}")
    private String repositoryName;          //备份仓库的名称

    @Value("${elasticsearch-backup.repository.location}")
    private String repositoryLocation;      //备份仓库的位置

    @Value("${elasticsearch-backup.snapshot}")
    private String snapshotName;            //快照名称

    @Value("${elasticsearch-backup.indices}")
    private String backupIndices;           //备份的索引
    
    @Value("${elasticsearch-backup.retry.backup}")
    private int backupFailRetryTimes;       //备份失败重试次数

    @Value("${elasticsearch-backup.retry.restore}")
    private int restoreFailRetryTimes;      //恢复失败重试次数
    
    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ProductIndexRepository productIndexRepository;

    /**
     * 创建备份仓库
     *
     * @return 创建结果
     * @throws IOException
     */
    private String init() throws IOException {
        if(!ElasticsearchSnapshotUtils.isRepositoryExist(elasticsearchUrl,repositoryName)){
            return ElasticsearchSnapshotUtils.createBackupRepository(elasticsearchUrl,repositoryName,repositoryLocation);
        }
        return "OK";
    }

    /**
     * 备份
     *
     * @return 备份结果
     * @throws IOException
     */
    public String backup() throws IOException {
        LOGGER.info("backup");
        init();
        if(ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)){
            delelteBackup();
        }
        return ElasticsearchSnapshotUtils.createSnapshot(elasticsearchUrl, repositoryName, snapshotName, backupIndices, false);
    }

    /**
     * 删除备份
     *
     * @return 删除备份结果
     * @throws IOException
     */
    public String delelteBackup() throws IOException {
        return ElasticsearchSnapshotUtils.deleteSnapshot(elasticsearchUrl, repositoryName, snapshotName);
    }

    /**
     * 获取当前索引的商品数量
     * @return 索引的商品数量
     */
    public long getProductIndexRepositoryCount() throws ServiceException {
       return productIndexRepository.count();
    }

    /**
     * 恢复备份
     *
     * @return 恢复结果
     * @throws ServiceException
     * @throws IOException
     */
    public String restore() throws ServiceException, IOException {
        LOGGER.info("restore");
        if(ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)){
            elasticsearchTemplate.deleteIndex(backupIndices);
            return ElasticsearchSnapshotUtils.restoreIndices(elasticsearchUrl, repositoryName, snapshotName, backupIndices);
        }
        return "FAIL";
    }

}
