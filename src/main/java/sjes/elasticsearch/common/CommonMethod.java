package sjes.elasticsearch.common;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CommonMethod {

    public static <T, K> List<K> listCopy(List<T> srcList, Class<K> clazz) throws IllegalAccessException, InstantiationException {
        if (CollectionUtils.isEmpty(srcList)) {
            return null;
        }
        List<K> kList = new ArrayList<>();
        int length = srcList.size();
        for (int i = 0; i < length; i++) {
            T src = srcList.get(i);
            Map<String, Object> srcMap = new HashMap<String, Object>();
            Class<?> srcClass = src.getClass();
            for (; srcClass != Object.class; srcClass = srcClass.getSuperclass()) {
                Field[] srcFields = srcClass.getDeclaredFields();
                for (Field fd : srcFields) {
                    try {
                        fd.setAccessible(true);
                        srcMap.put(fd.getName(), fd.get(src)); //获取属性值
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            K dest = clazz.newInstance();
            Class<?> destClass = dest.getClass();
            for (; destClass != Object.class; destClass = destClass.getSuperclass()) {
                Field[] destFields = destClass.getDeclaredFields();
                String typeClass;
                for (Field fd : destFields) {
                    Object value = srcMap.get(fd.getName());
                    if (value == null) {
                        continue;
                    }
                    try {
                        typeClass = fd.getType().getSimpleName();

                        if (typeClass.equals("Integer")) {
                            value = Integer.parseInt(value.toString());
                        } else if (typeClass.equals("String")) {
                            value = value.toString();
                        }
                        fd.setAccessible(true);
                        fd.set(dest, value); //给属性赋值
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            kList.add(dest);
        }
        return kList;
    }
}
