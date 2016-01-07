package sjes.elasticsearch.utils;

import okhttp3.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

/**
 * Created by 白 on 2016/1/7.
 */
public class ElasticsearchSnapshotUtils {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    static{
        client = new OkHttpClient();
    }

    /**
     * 创建备份仓库
     *
     * POST http://127.0.0.1:9200/_snapshot/my_backup/
     * {
     *      "type": "fs",
     *      "settings": {
     *          "location": "/mount/backups/my_backup"
     *      }
     * }
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param location 存储位置
     * @return 创建结果
     */
    public static String createBackupRepository(String esUrl, String repositoryName, String location) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/";

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("type", "fs")
                .startObject("settings")
                .field("location", location)
                .endObject()
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 创建备份仓库
     *
     * POST http://127.0.0.1:9200/_snapshot/my_backup/
     * {
     *      "type": "fs",
     *      "settings": {
     *          "location": "/mount/backups/my_backup",
     *          "max_snapshot_bytes_per_sec" : "50mb",
     *          "max_restore_bytes_per_sec" : "50mb"
     *      }
     * }
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param location 存储位置
     * @param maxSnapshotBytesPerSec 最大创建快照速率(b/s)
     * @param maxRestoreBytesPerSec 最大恢复速率(b/s)
     * @return 创建结果
     */
    public static String createBackupRepository(String esUrl, String repositoryName, String location,
                                              String maxSnapshotBytesPerSec,String maxRestoreBytesPerSec) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/";

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("type", "fs")
                .startObject("settings")
                .field("location", location)
                .field("max_snapshot_bytes_per_sec", maxSnapshotBytesPerSec)
                .field("max_restore_bytes_per_sec", maxRestoreBytesPerSec)
                .endObject()
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 判断仓库是否存在
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @return 存在true，不存在false
     * @throws IOException
     */
    public static boolean isRepositoryExist(String esUrl, String repositoryName) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/";

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    /**
     * 创建快照
     *
     * PUT http://127.0.0.1:9200/_snapshot/my_backup/snapshot_2
     * {
     *      "indices": "index_1,index_2"
     * }
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param snapshotName 快照名称
     * @param indices 创建快照的索引（index_1,index_2）
     * @param isAsync 是否异步
     * @return 创建结果
     */
    public static String createSnapshot(String esUrl, String repositoryName, String snapshotName, String indices, boolean isAsync) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/" + snapshotName;

        if(!isAsync){
            url += "?wait_for_completion=true";
        }

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("indices", indices)
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).put(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 判断快照是否存在
     *
     * GET http://127.0.0.1:9200/_snapshot/my_backup/snapshot_1
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @return 存在true，不存在false
     * @throws IOException
     */
    public static boolean isSnapshotExist(String esUrl, String repositoryName, String snapshot) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/" + snapshot;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    /**
     * 删除快照
     *
     * DELETE http://127.0.0.1:9200/_snapshot/my_backup/snapshot_1
     *
     * @param esUrl
     * @param repositoryName
     * @param snapshot
     * @return
     * @throws IOException
     */
    public static String deleteSnapshot(String esUrl, String repositoryName, String snapshot) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/" + snapshot;

        Request request = new Request.Builder().url(url).delete().build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 恢复索引
     *
     * POST http://127.0.0.1:9200/_snapshot/my_backup/snapshot_1/_restore
     * {
     *      "indices": "index_1"
     * }
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param snapshotName 快照名称
     * @param indices 需要恢复的索引
     * @return 创建结果
     */
    public static String restoreIndices(String esUrl, String repositoryName, String snapshotName, String indices) throws IOException {

        String url = esUrl + "/_snapshot/"+ repositoryName + "/" + snapshotName + "/_restore";

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("indices", indices)
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
