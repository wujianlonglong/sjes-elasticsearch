package sjes.elasticsearch.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domain.ProductIndexNew;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
public interface ProductIndexNewRepository extends ElasticsearchRepository<ProductIndexNew, Long> {

    /**
     * 根据商品编号查询 ProductIndex
     *
     * @param sn 商品编号
     * @return ProductIndex
     */
    ProductIndexNew findBySn(String sn);


    /**
     * 根据ERPGOODSID查询 ProductIndex.
     */
    ProductIndexNew findByErpGoodsId(Long erpGoodsId);

    Page<ProductIndexNew> findByErpGoodsIdIn(List<Long> erpGoodsIds, Pageable pageable);

}
