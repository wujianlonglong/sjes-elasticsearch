package sjes.elasticsearch.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by 白 on 2016/3/4.
 */
public class PinYinUtils {

    private static final String EMPTY = "";

    /** 大写输出 */
    private static final HanyuPinyinOutputFormat OUTPUT_FORMAT = new HanyuPinyinOutputFormat();
    static {
        OUTPUT_FORMAT.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        OUTPUT_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 汉字转拼音的方法
     *
     * 如好丽友好多鱼(浓香茄汁)33g转换成HAOLIYOUHAODUOYU(NONGXIANGQIEZHI)33g
     *
     * @param chineseCharacters
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    @SuppressWarnings("deprecation")
    public static String formatToPinYin(String chineseCharacters)
            throws BadHanyuPinyinOutputFormatCombination {
        if (null == chineseCharacters || EMPTY.equals(chineseCharacters.trim()))
            return chineseCharacters;

        return PinyinHelper.toHanyuPinyinString(chineseCharacters,
                OUTPUT_FORMAT, EMPTY);
    }

    /**
     * 汉字转拼音的方法
     *
     * 如：好丽友好多鱼(浓香茄汁)33g转换成HLYHDY(NXQZ)33g
     *
     * @param chineseCharacters
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    public static String formatAbbrToPinYin(String chineseCharacters)
            throws BadHanyuPinyinOutputFormatCombination {
        if (null == chineseCharacters || EMPTY.equals(chineseCharacters.trim()))
            return chineseCharacters;

        char[] chars = chineseCharacters.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (c > 127) {
                sb.append(PinyinHelper.toHanyuPinyinStringArray(c,
                        OUTPUT_FORMAT)[0].toCharArray()[0]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
