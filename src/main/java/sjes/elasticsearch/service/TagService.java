package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.category.feign.TagFeign;
import sjes.elasticsearch.feigns.category.model.Tag;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
@Service("tagService")
public class TagService {

    @Autowired
    private TagFeign tagFeign;

    /**
     * 查询所有的标签信息
     * @return 所有的标签信息
     */
    public List<Tag> all() {
        return tagFeign.all();
    }
}
