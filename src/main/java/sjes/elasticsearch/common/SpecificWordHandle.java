package sjes.elasticsearch.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by 白 on 2016/3/3.
 *
 * 对特定搜索词进行处理
 */
public class SpecificWordHandle {
    public static HashSet<String> shouldMatchNames;                     //名字中不带搜索词的商品,如搜索『水果』需要出来『苹果』等商品
    public static HashMap<String, String> specificWords;                //将特殊的搜索词进行转换,如搜索『奶』出来『牛奶』的搜索结果
    public static HashSet<String> specificBrandNames;                   //可能为品牌的关键词，如搜索『小浣熊』出来『统一小浣熊方便面』和『小浣熊倍润唇膏』
    public static HashMap<String, String> similarNames;                 //同义词名称搜索,如搜索『酸奶』出来『酸奶』和『酸牛奶』的搜索结果
    public static HashMap<String, String> similarTags;                  //同义词标签搜索,如搜索『牛奶』搜索结果中包括『标签为含乳饮料的商品』
    public static HashMap<String, HashSet<Long>> specificCategories;    //指定分类,搜索结果必须为此分类下的商品
    public static HashMap<String, HashSet<Long>> exceptCategories;      //排除分类,搜索结果包括此分类下的商品

    static {
        shouldMatchNames = new HashSet<>();
        specificWords = new HashMap<>();
        specificCategories = new HashMap<>();
        specificBrandNames = new HashSet<>();
        similarNames = new HashMap<>();
        similarTags = new HashMap<>();
        exceptCategories = new HashMap<>();


        shouldMatchNames.addAll(Arrays.asList("水果", "蔬菜", "生鲜"));


        specificWords.put("奶", "牛奶");
        specificWords.put("米", "大米");
        specificWords.put("姨妈巾", "卫生巾");
        specificWords.put("吃哒", "休闲食品");
        specificWords.put("吃的", "休闲食品");
        specificWords.put("RIO", "锐澳");
        specificWords.put("POCKY", "格力高百奇");
        specificWords.put("糖", "糖果");


        similarNames.put("酸奶", "酸牛奶");
        similarNames.put("酸牛奶", "酸奶");
        similarNames.put("雨伞", "伞");


        similarTags.put("奶", "含乳饮料");
        similarTags.put("牛奶", "含乳饮料");
        similarTags.put("番茄酱", "调味酱");
        similarTags.put("番茄沙司", "调味酱");
        similarTags.put("零食", "休闲食品");
        similarTags.put("酸牛奶", "酸奶");


        addSpecificCategories("大米", Arrays.asList(124L));
        addSpecificCategories("米", Arrays.asList(124L, 128L));
        addSpecificCategories("油", Arrays.asList(125L));
        addSpecificCategories("纸巾", Arrays.asList(324L, 327L));
        addSpecificCategories("牛奶", Arrays.asList(2L, 82L, 103L, 155L, 174L, 164L, 195L));
        addSpecificCategories("酸奶", Arrays.asList(174L, 164L));
        addSpecificCategories("酸牛奶", Arrays.asList(174L, 164L));
        addSpecificCategories("水", Arrays.asList(165L));
        addSpecificCategories("矿泉水", Arrays.asList(165L));
        addSpecificCategories("纯净水", Arrays.asList(165L));
        addSpecificCategories("蒸馏水", Arrays.asList(165L));
        addSpecificCategories("水果", Arrays.asList(640L, 641L));
        addSpecificCategories("蔬菜", Arrays.asList(638L));
        addSpecificCategories("生鲜", Arrays.asList(637L));
        addSpecificCategories("面", Arrays.asList(57L, 113L, 126L));
        addSpecificCategories("洗面奶", Arrays.asList(249L));


        specificBrandNames.add("小浣熊");
        specificBrandNames.add("海洋");
        specificBrandNames.add("小助手");
        specificBrandNames.add("阳光");
        specificBrandNames.add("理想");
        specificBrandNames.add("无");


        addExceptCategories("牛奶", Arrays.asList(170L, 653L, 129L, 416L, 83L, 305L));
        addExceptCategories("奶", Arrays.asList(170L));
        addExceptCategories("奶粉", Arrays.asList(134L, 127L, 335L, 113L, 186L, 286L, 206L, 185L));
        addExceptCategories("薯片", Arrays.asList(189L));
        addExceptCategories("薯条", Arrays.asList(113L));
    }

    /**
     * 添加指定分类
     */
    private static void addSpecificCategories(String keyword, Collection<Long> categories) {
        HashSet<Long> tmpSpecificCategories = new HashSet<>();
        tmpSpecificCategories.addAll(categories);
        specificCategories.put(keyword, tmpSpecificCategories);
    }

    /**
     * 添加排除分类
     */
    private static void addExceptCategories(String keyword, Collection<Long> categories) {
        HashSet<Long> tmpExceptCategories = new HashSet<>();
        tmpExceptCategories.addAll(categories);
        exceptCategories.put(keyword, tmpExceptCategories);
    }
}
