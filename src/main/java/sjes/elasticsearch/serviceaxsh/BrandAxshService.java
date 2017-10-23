package sjes.elasticsearch.serviceaxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sjes.elasticsearch.feigns.item.feignaxsh.BrandAxshFeign;
import sjes.elasticsearch.feigns.item.model.Brand;

import java.util.List;

/**
 * Created by qinhailong on 15-12-25.
 */
@Service("brandAxshService")
public class BrandAxshService {

    @Autowired
    private BrandAxshFeign brandAxshFeign;

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    public List<Brand> listAll() {
        return brandAxshFeign.listAll();
    }

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    public Brand get(Long id) {
        return brandAxshFeign.get(id);
    }

}
