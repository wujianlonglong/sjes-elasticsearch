package sjes.elasticsearch.serviceaxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;

import java.util.List;

/**
 * Created by qinhailong on 15-12-8.
 */
@Service("productIndexAxshService")
public class ProductIndexAxshService {

    @Autowired
    private ProductIndexAxshRepository  productIndexAxshRepository;

    /**
     * 批量保存ProductIndex信息
     * @param productIndexes ProductIndex信息
     */
    public void saveBat(List<ProductIndexAxsh> productIndexes) {
        productIndexAxshRepository.save(productIndexes);
    }
}
