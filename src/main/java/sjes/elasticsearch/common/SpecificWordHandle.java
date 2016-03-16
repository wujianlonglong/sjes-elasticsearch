package sjes.elasticsearch.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by 白 on 2016/3/3.
 *
 * 对特定搜索词进行处理
 */
public class SpecificWordHandle {
    public static HashMap<String, String> specificWords;        //将特殊的搜索词进行转换
    public static HashMap<String, HashSet<String>> specificBrandName;   //搜索词增加品牌，比如小浣熊牌和统一牌小浣熊
    public static HashMap<String, String> similarWords;         //同义词标签搜索(如 牛奶->搜含乳饮料标签)
    public static HashMap<String, HashSet<Long>> specificCategories;     //指定分类
    public static HashMap<String, HashSet<Long>> exceptCategories;     //排除分类

    static {
        specificWords = new HashMap<>();
        specificCategories = new HashMap<>();
        specificBrandName = new HashMap<>();
        similarWords = new HashMap<>();
        exceptCategories = new HashMap<>();


        specificWords.put("水", "饮用水");
        specificWords.put("奶", "牛奶");
        specificWords.put("米", "大米");


        similarWords.put("奶", "含乳饮料");
        similarWords.put("牛奶", "含乳饮料");
        similarWords.put("番茄酱", "调味酱");
        similarWords.put("番茄沙司", "调味酱");


        HashSet<Long> riceCategories = new HashSet<>();
        riceCategories.add(124L);
        specificCategories.put("大米", riceCategories);
        riceCategories = new HashSet<>();
        riceCategories.addAll(Arrays.asList(124L, 128L));
        specificCategories.put("米", riceCategories);

        HashSet<Long> oilCategories = new HashSet<>();
        oilCategories.add(125L);
        specificCategories.put("油", oilCategories);

        HashSet<Long> tissueCategories = new HashSet<>();
        tissueCategories.addAll(Arrays.asList(324L, 327L));
        specificCategories.put("纸巾", tissueCategories);


        HashSet<String> xhxBrandName = new HashSet<>();
        xhxBrandName.add("统一");
        specificBrandName.put("小浣熊", xhxBrandName);


        HashSet<Long> notMilkCategories = new HashSet<>();
        notMilkCategories.add(170L);
        exceptCategories.put("牛奶", notMilkCategories);
        exceptCategories.put("奶", notMilkCategories);

        HashSet<Long> notPowderedMilkCategories = new HashSet<>();
        notPowderedMilkCategories.addAll(Arrays.asList(134L, 127L, 335L, 113L, 186L, 286L, 206L, 185L));
        exceptCategories.put("奶粉", notPowderedMilkCategories);
    }
}
