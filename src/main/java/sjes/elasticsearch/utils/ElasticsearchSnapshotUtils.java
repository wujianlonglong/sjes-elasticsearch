package sjes.elasticsearch.utils;

import okhttp3.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sjes.elasticsearch.service.SearchService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 白 on 2016/1/7.
 */
public class ElasticsearchSnapshotUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    static {
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
     * @return 创建成功true,创建失败false
     */
    public static boolean createBackupRepository(String esUrl, String repositoryName, String location) throws IOException {

        String url = esUrl + "/_snapshot/" + repositoryName + "/";

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("type", "fs")
                .startObject("settings")
                .field("location", location)
                .endObject()
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return isResponseAcknowledgedTrue(response);
    }

    /**
     * 删除仓库
     *
     * POST http://127.0.0.1:9200/_snapshot/my_backup/
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @return 是否成功删除
     * @throws IOException
     */
    public static boolean deleteRepository(String esUrl, String repositoryName) throws IOException {

        String url = esUrl + "/_snapshot/" + repositoryName + "/";

        Request request = new Request.Builder().url(url).delete().build();
        Response response = client.newCall(request).execute();

        return isResponseAcknowledgedTrue(response) || !isRepositoryExist(esUrl, repositoryName);
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

        String url = esUrl + "/_snapshot/" + repositoryName + "/";

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
    public static boolean createSnapshot(String esUrl, String repositoryName, String snapshotName, String indices, boolean isAsync) throws IOException {

        String url = esUrl + "/_snapshot/" + repositoryName + "/" + snapshotName;

        if (!isAsync) {
            url += "?wait_for_completion=true";
        }

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("indices", indices)
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).put(body).build();
        Response response = client.newCall(request).execute();

        XContentParser parser = null;
        boolean result = false;

        try {
            parser = XContentFactory.xContent(XContentType.JSON).createParser(response.body().string());
            Map<String, Object> responseParsed = parser.mapAndClose();

            if(responseParsed.containsKey("snapshot") && responseParsed.get("snapshot") != null){
                HashMap snapshot = (HashMap)responseParsed.get("snapshot");
                if(snapshot.containsKey("state") && snapshot.get("state").equals("SUCCESS")){
                    result = true;
                }
            }
        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return result;
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

        String url = esUrl + "/_snapshot/" + repositoryName + "/" + snapshot;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    /**
     * 删除快照
     *
     * DELETE http://127.0.0.1:9200/_snapshot/my_backup/snapshot_1
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param snapshot 快照名称
     * @return 快照是否存在
     * @throws IOException
     */
    public static boolean deleteSnapshot(String esUrl, String repositoryName, String snapshot) throws IOException {

        String url = esUrl + "/_snapshot/" + repositoryName + "/" + snapshot;

        Request request = new Request.Builder().url(url).delete().build();
        Response response = client.newCall(request).execute();

        return isResponseAcknowledgedTrue(response) || !isSnapshotExist(esUrl, repositoryName, snapshot);
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
     * @return 恢复结果
     */
    public static boolean restoreIndices(String esUrl, String repositoryName, String snapshotName, String indices) throws IOException {

        String url = esUrl + "/_snapshot/" + repositoryName + "/" + snapshotName + "/_restore";

        XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                .field("indices", indices)
                .endObject();

        RequestBody body = RequestBody.create(JSON, content.string());
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return isResponseAcceptedTrue(response);
    }

    /**
     * 判断接口调用返回的结果中是否有acknowledged参数且结果为true
     *
     * @return acknowledged是否为true
     */
    private static boolean isResponseAcknowledgedTrue(Response response) throws IOException {

        if(!response.isSuccessful()){
            return false;
        }

        boolean result = false;
        XContentParser parser = null;

        try {
            parser = XContentFactory.xContent(XContentType.JSON).createParser(response.body().string());
            Map<String, Object> responseParsed = parser.mapAndClose();
            if (responseParsed.containsKey("acknowledged") && (Boolean) responseParsed.get("acknowledged")) {
                result = true;
            }
        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return result;
    }

    /**
     * 判断接口调用返回的结果中是否有accepted参数且结果为true
     *
     * @return accepted是否为true
     */
    private static boolean isResponseAcceptedTrue(Response response) throws IOException {

        if(!response.isSuccessful()){
            return false;
        }

        boolean result = false;
        XContentParser parser = null;

        try {
            parser = XContentFactory.xContent(XContentType.JSON).createParser(response.body().string());
            Map<String, Object> responseParsed = parser.mapAndClose();
            if (responseParsed.containsKey("accepted") && (Boolean) responseParsed.get("accepted")) {
                result = true;
            }
        } finally {
            if (null != parser) {
                parser.close();
            }
        }

        return result;
    }

    /**
     * 获取快照相关信息
     *
     * POST http://127.0.0.1:9200/_snapshot/my_backup/snapshot_1/_restore
     * {
     *      "indices": "index_1"
     * }
     *
     * @param esUrl elasticsearch节点地址（http://127.0.0.1:9200）
     * @param repositoryName 仓库名称
     * @param snapshotName 快照名称
     * @return 快照相关信息
     * @throws IOException
     */
    public static String getSnapshotInfo(String esUrl, String repositoryName, String snapshotName) throws IOException {
        String url = esUrl + "/_snapshot/" + repositoryName + "/" + snapshotName;

        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
