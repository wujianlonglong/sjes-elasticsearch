package sjes.elasticsearch.common;

import java.util.HashMap;

/**
 * Created by 白 on 2016/3/3.
 *
 * 对特定搜索词进行处理
 */
public class SpecificWordHandle {
    public static HashMap<String, String> specificWords;

    static {
        specificWords = new HashMap<>();

        specificWords.put("米", "米类");
        specificWords.put("大米", "米类");
        specificWords.put("水", "饮用水");
        specificWords.put("油", "食用油");
        specificWords.put("奶", "牛奶");
    }
}
