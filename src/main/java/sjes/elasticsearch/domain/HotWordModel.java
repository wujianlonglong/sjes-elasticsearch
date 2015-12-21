package sjes.elasticsearch.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by 白 on 2015/12/21.
 */
@Data
public class HotWordModel implements Serializable {

    private String keyword;     //关键词
    private long count;          //查询次数
}
