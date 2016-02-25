package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.feigns.item.feign.BrandFeign;
import sjes.elasticsearch.feigns.item.model.Brand;

import java.util.List;

/**
 * Created by qinhailong on 15-12-25.
 */
@Service("brandService")
public class BrandService {

    @Autowired
    private BrandFeign brandFeign;

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    public List<Brand> listAll() {
        return brandFeign.listAll();
    }

    /**
     * 查询所有品牌信息
     * @return 品牌列表
     */
    public Brand get(Long id) {
        return brandFeign.get(id);
    }

}
