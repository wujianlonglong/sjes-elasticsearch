package sjes.elasticsearch.serviceaxsh;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.repository.CategoryRepository;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.service.SearchService;
import sjes.elasticsearch.utils.ElasticsearchSnapshotUtils;
import sjes.elasticsearch.utils.LogWriter;

import java.io.IOException;

/**
 * Created by 白 on 2016/1/7.
 */
@Service("backupAxshService")
public class BackupAxshService {

    @Value("${elasticsearchbackup.url}")
    private String elasticsearchUrl;        //elasticsearch 地址

    @Value("${elasticsearchbackup.repositoryaxsh.name}")
    private String repositoryName;          //axsh备份仓库的名称

    @Value("${elasticsearchbackup.repositoryaxsh.location}")
    private String repositoryLocation;      //axsh备份仓库的位置

    @Value("${elasticsearchbackup.snapshotaxsh}")
    private String snapshotName;            //axsh快照名称

    @Value("${elasticsearchbackup.axshindex}")
    private String backupIndices;           //备份的索引

    @Value("${elasticsearchbackup.checkindexcount.product}")
    private int checkProductCount;        //检查索引的产品数量

    @Value("${elasticsearchbackup.checkindexcount.category}")
    private int checkCategoryCount;        //检查索引的分类数量

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ProductIndexAxshRepository productIndexAxshRepository;

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
    public boolean backup() throws ServiceException {
        LogWriter.append("backup", "start");

        boolean isSucceed = false;           //备份是否成功

        if(isIndexVaild()) {

            try {
                init();
                if (ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)) {
                    delelteBackup();
                }
                isSucceed = ElasticsearchSnapshotUtils.createSnapshot(elasticsearchUrl, repositoryName, snapshotName, backupIndices, false);
            } catch (IOException e) {
                throw new ServiceException(ExceptionUtils.getMessage(e), ExceptionUtils.getRootCause(e));
            }
        }
        LogWriter.append("backup", isSucceed?"success":"fail");
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
//    public boolean isIndexVaild(){
//        return elasticsearchTemplate.indexExists(backupIndices)             //判断索引存在
//                && productIndexAxshRepository.count() > checkProductCount       //判断索引的商品数量
//                && categoryRepository.count() > checkCategoryCount;         //判断索引的分类数量
//    }
    public boolean isIndexVaild(){
        return elasticsearchTemplate.indexExists(backupIndices);             //判断索引存在
    }

    /**
     * 判断索引是否存在
     *
     * @return 是true,否false
     */
    public boolean isIndexExists() {
        return elasticsearchTemplate.indexExists(backupIndices);
    }

    /**
     * 恢复备份
     *
     * @return 恢复结果
     * @throws ServiceException
     * @throws IOException
     */
    public boolean restore() throws ServiceException {
        LogWriter.append("restore", "start");

        boolean isSucceed = false;
        try {
            if (ElasticsearchSnapshotUtils.isSnapshotExist(elasticsearchUrl, repositoryName, snapshotName)) {
                elasticsearchTemplate.deleteIndex(backupIndices);
                isSucceed = ElasticsearchSnapshotUtils.restoreIndices(elasticsearchUrl, repositoryName, snapshotName, backupIndices)
                                && elasticsearchTemplate.indexExists(backupIndices);
            }
        } catch (IOException e) {
            throw new ServiceException(ExceptionUtils.getMessage(e), ExceptionUtils.getRootCause(e));
        }

        LogWriter.append("restore", isSucceed?"success":"fail");
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
