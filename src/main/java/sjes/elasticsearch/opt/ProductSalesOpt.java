package sjes.elasticsearch.opt;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domain.ProductSales;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;
import sjes.elasticsearch.service.SearchService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSalesOpt {

    private static Logger log = LoggerFactory.getLogger(ProductSalesOpt.class);

    @Autowired
    RestTemplate restTemplate;

    private static final String productSalesUrl = "srv0.sanjiang.info:20060/orders/outservice/sellList";


    @Autowired
    ProductIndexAxshRepository productIndexAxshRepository;


    @Autowired
    ProductIndexRepository productIndexRepository;

    @Autowired
    SearchService searchService;

    @Autowired
    SearchAxshService searchAxshService;

    @Scheduled(cron = "0 */30 7-19 * * ?")
    public void ProductSalesSync() {
        log.info("同步商品销量开始-------" + LocalDateTime.now());
        try {
            List<ProductSales> productSalesList = restTemplate.getForObject(productSalesUrl, ArrayList.class);
            if (CollectionUtils.isEmpty(productSalesList)) {
                return;
            }
            Map<String, Long> cxllProductSalesMap = new HashMap<>();
            Map<String, Long> sjejProductSalesMap = new HashMap<>();
            for (ProductSales productSales : productSalesList) {
                String platId = productSales.getPlatId();
                String goodId = productSales.getGoodId();
                Long saleNum = productSales.getSaleNum();
                if (platId == "10004") {
                    if (sjejProductSalesMap.containsKey(goodId)) {
                        sjejProductSalesMap.put(goodId, sjejProductSalesMap.get(goodId) + saleNum);
                    } else {
                        sjejProductSalesMap.put(goodId, saleNum);
                    }
                } else if (platId == "10005") {
                    if (cxllProductSalesMap.containsKey(goodId)) {
                        cxllProductSalesMap.put(goodId, cxllProductSalesMap.get(goodId) + saleNum);
                    } else {
                        cxllProductSalesMap.put(goodId, saleNum);
                    }
                }
            }

            if (!MapUtils.isEmpty(cxllProductSalesMap)) {
                List<ProductIndexAxsh> productIndexAxshList = searchAxshService.findBySnInAxsh(new ArrayList<String>(cxllProductSalesMap.keySet()));
                for (ProductIndexAxsh productIndexAxsh : productIndexAxshList) {
                    productIndexAxsh.setSales(productIndexAxsh.getSales() + cxllProductSalesMap.get(productIndexAxsh.getErpGoodsId()));
                }
                productIndexAxshRepository.save(productIndexAxshList);
            }
            if (!MapUtils.isEmpty(sjejProductSalesMap)) {
                List<ProductIndex> productIndexList = searchService.findBySnIn(new ArrayList<String>(sjejProductSalesMap.keySet()));
                for (ProductIndex productIndex : productIndexList) {
                    productIndex.setSales(productIndex.getSales() + sjejProductSalesMap.get(productIndex.getErpGoodsId()));
                }
                productIndexRepository.save(productIndexList);
            }

        } catch (Exception ex) {
            log.error("同步商品销量失败：" + ex.toString());
            return;
        } finally {
            log.info("同步商品销售结束-------" + LocalDateTime.now());
        }

    }

}
