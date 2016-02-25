package sjes.elasticsearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.repository.CategoryRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.utils.ElasticsearchSnapshotUtils;
import sun.rmi.runtime.Log;

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

    @Value("${elasticsearch-backup.check-index-count.product}")
    private int checkProductCount;        //检查索引的产品数量

    @Value("${elasticsearch-backup.check-index-count.category}")
    private int checkCategoryCount;        //检查索引的分类数量

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ProductIndexRepository productIndexRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 创建备份仓库
     *
     * @return 创建结果
     * @throws IOException
     */
    public boolean init() throws IOException {
        return ElasticsearchSnapshotUtils.isRepositoryExist(elasticsearchUrl, repositoryName) ||
                ElasticsearchSnapshotUtils.createBackupRepository(elasticsearchUrl, repositoryName, repositoryLocation);
    }

    /**
     * 备份
     *
     * @return 备份结果
     * @throws IOException
     */
    public boolean backup() throws IOException {
        boolean isSucceed = false;           //备份是否成功

        LOGGER.info("start backup");
        if(isIndexVaild()) {
            init();
            if (ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)) {
                delelteBackup();
            }
            isSucceed = ElasticsearchSnapshotUtils.createSnapshot(elasticsearchUrl, repositoryName, snapshotName, backupIndices, false);
        }

        LOGGER.info("backup " + (isSucceed?"success":"fail"));
        return isSucceed;
    }

    /**
     * 删除备份
     *
     * @return 删除备份结果
     * @throws IOException
     */
    public boolean delelteBackup() throws IOException {
        return ElasticsearchSnapshotUtils.deleteSnapshot(elasticsearchUrl, repositoryName, snapshotName);
    }

    /**
     * 判断当前索引是否有效
     *
     * @return 有效true，无效false
     */
    public boolean isIndexVaild(){
        return productIndexRepository.count() > checkProductCount && categoryRepository.count() > checkCategoryCount;
    }

    /**
     * 恢复备份
     *
     * @return 恢复结果
     * @throws ServiceException
     * @throws IOException
     */
    public boolean restore() throws ServiceException, IOException {
        LOGGER.info("start restore");
        boolean isSucceed = false;
        if (ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)) {
            elasticsearchTemplate.deleteIndex(backupIndices);
            isSucceed = ElasticsearchSnapshotUtils.restoreIndices(elasticsearchUrl, repositoryName, snapshotName, backupIndices) && isIndexVaild();
        }
        LOGGER.info("restore :" + (isSucceed?"success":"fail"));
        return isSucceed;
    }

    /**
     * 获取快照的信息
     *
     * @return 快照信息
     * @throws ServiceException
     * @throws IOException
     */
    public String getSnapshotNameInfo() throws ServiceException, IOException {
        return ElasticsearchSnapshotUtils.getSnapshotInfo(elasticsearchUrl, repositoryName, snapshotName);
    }
}