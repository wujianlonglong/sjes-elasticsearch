package sjes.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import sjes.elasticsearch.domain.ProductIndex;

/**
 * Created by qinhailong on 15-12-8.
 */
public interface ProductIndexRepository extends ElasticsearchRepository<ProductIndex, Long> {

    /**
     * 根据商品编号查询 ProductIndex
     *
     * @param sn 商品编号
     * @return ProductIndex
     */
    ProductIndex findBySn(String sn);

    /**
     * 根据ERPGOODSID查询 ProductIndex.
     */
    ProductIndex findByErpGoodsId(Long erpGoodsId);
}
