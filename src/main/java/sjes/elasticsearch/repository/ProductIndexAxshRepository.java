package sjes.elasticsearch.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sjes.elasticsearch.domainAxsh.ProductIndexAxsh;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
public interface ProductIndexAxshRepository extends ElasticsearchRepository<ProductIndexAxsh, Long> {

    /**
     * 根据商品编号查询 ProductIndexAxsh
     *
     * @param sn 商品编号
     * @return ProductIndexAxsh
     */
    ProductIndexAxsh findBySn(String sn);

    Page<ProductIndexAxsh> findBySnIn(List<String> sns, Pageable pageable);


    /**
     * 根据ERPGOODSID查询 ProductIndexAxsh.
     */
    ProductIndexAxsh findByErpGoodsId(Long erpGoodsId);

    Page<ProductIndexAxsh> findByErpGoodsIdIn(List<Long> erpGoodsIds, Pageable pageable);

}
