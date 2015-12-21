package sjes.elasticsearch.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by qinhailong on 15-12-7.
 */
public class ListUtils {

    /**
     * 分拆列表大小
     */
    public static final int SPLIT_SUB_LIST_SIZE = 50;

    /**
     * 拆分 list
     * @param list list列表
     * @param subListSize 列表大小
     * @param <E>
     * @return
     */
    public static <E> List<List<E>> splitList(List<E> list, int subListSize){
        List<List<E>> resultList = Lists.newArrayList();
        if (subListSize <= 0) {
            subListSize = SPLIT_SUB_LIST_SIZE;
        }
        if(CollectionUtils.isNotEmpty(list)){
            int sizeOfList = list.size();
            int count = sizeOfList / subListSize;
            int mod = sizeOfList % subListSize;
            for (int i = 0; i <= count; i++) {
                List<E> subList = null;
                if(i != count) {
                    subList = list.subList(i * subListSize, subListSize*(i+1));
                }else if(mod > 0){
                    subList = list.subList(i * subListSize, i * subListSize + mod);
                }
                if(CollectionUtils.isNotEmpty(subList)){
                    resultList.add(subList);
                }
            }

        }
        return resultList;
    }
}
