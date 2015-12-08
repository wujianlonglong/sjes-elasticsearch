package sjes.elasticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.repository.ProductIndexRepository;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
@Service("productIndexService")
public class ProductIndexService {

    @Autowired
    private ProductIndexRepository productIndexRepository;

    /**
     * 批量保存ProductIndex信息
     * @param productIndexes ProductIndex信息
     */
    public void saveBat(List<ProductIndex> productIndexes) {
        productIndexRepository.save(productIndexes);
    }
}
