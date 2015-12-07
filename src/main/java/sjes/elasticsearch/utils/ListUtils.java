package sjes.elasticsearch.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by qinhailong on 15-12-7.
 */
public class ListUtils {

    /**
     * 拆分 list
     * @param list
     * @param sub_list_size 列表大小
     * @param <E>
     * @return
     */
    public static <E> List<List<E>> splitList(List<E> list, int sub_list_size){
        List<List<E>> resultList = Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(list)){
            int sizeOfList = list.size();
            int count = sizeOfList / sub_list_size;
            int mod = sizeOfList % sub_list_size;
            for (int i = 0; i <= count; i++) {
                List<E> subList = null;
                if(i != count) {
                    subList = list.subList(i * sub_list_size, sub_list_size*(i+1));
                }else if(mod > 0){
                    subList = list.subList(i * sub_list_size, i * sub_list_size + mod);
                }
                if(CollectionUtils.isNotEmpty(subList)){
                    resultList.add(subList);
                }
            }

        }
        return resultList;
    }
}
