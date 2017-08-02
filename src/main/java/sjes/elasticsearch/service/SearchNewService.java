package sjes.elasticsearch.service;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.Pageable;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domain.ProductIndexNew;
import sjes.elasticsearch.repository.ProductIndexNewRepository;

import java.util.LinkedList;
import java.util.List;

@Service("searchNewService")
public class SearchNewService {


    @Autowired
    ProductIndexNewRepository productIndexNewRepository;


    public List<ProductIndexNew> findByErpGoodsIdIn(List<Long> erpGoodsIds){
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, 999);
        Page<ProductIndexNew> productIndexPage = productIndexNewRepository.findByErpGoodsIdIn(erpGoodsIds, pageable);
        return productIndexPage.getContent();
    }


    public PageModel<ProductIndexNew> listProductIndex(LinkedList<Long> erpGoodsIds, Integer page, Integer size) {
        org.springframework.data.domain.Pageable pageable = new PageRequest(page, size);
        if (CollectionUtils.isNotEmpty(erpGoodsIds)) {
            Page<ProductIndexNew> productIndexPage = productIndexNewRepository.findByErpGoodsIdIn(erpGoodsIds, pageable);
            return new PageModel<>(productIndexPage.getContent(), productIndexPage.getTotalElements(), new Pageable(productIndexPage.getNumber(), productIndexPage.getSize()));
        }
        return new PageModel<>(null, 0, new Pageable(1, size));
    }

}
