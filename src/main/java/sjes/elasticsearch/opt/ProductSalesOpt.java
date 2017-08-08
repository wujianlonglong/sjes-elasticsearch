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
import sjes.elasticsearch.feigns.order.feign.SellFeign;
import sjes.elasticsearch.feigns.order.model.ProductSales;
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

    @Autowired
    SellFeign sellFeign;

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
            List<ProductSales> productSalesList=sellFeign.getSellList();
            if (CollectionUtils.isEmpty(productSalesList)) {
                return;
            }
            Map<Long, Long> cxllProductSalesMap = new HashMap<>();
            Map<Long, Long> sjejProductSalesMap = new HashMap<>();
            for (ProductSales productSales : productSalesList) {
                String platId = productSales.getPlatId();
                Long goodId = productSales.getErpGoodsId();
                Long saleNum = productSales.getSaleNum();
                if (platId.equals("10004")) {
                    if (sjejProductSalesMap.containsKey(goodId)) {
                        sjejProductSalesMap.put(goodId, sjejProductSalesMap.get(goodId) + saleNum);
                    } else {
                        sjejProductSalesMap.put(goodId, saleNum);
                    }
                } else if (platId.equals("10005")) {
                    if (cxllProductSalesMap.containsKey(goodId)) {
                        cxllProductSalesMap.put(goodId, cxllProductSalesMap.get(goodId) + saleNum);
                    } else {
                        cxllProductSalesMap.put(goodId, saleNum);
                    }
                }
            }

            if (!MapUtils.isEmpty(cxllProductSalesMap)) {
                List<ProductIndexAxsh> productIndexAxshList = searchAxshService.findByErpGoodsIdIn(new ArrayList<Long>(cxllProductSalesMap.keySet()));
                for (ProductIndexAxsh productIndexAxsh : productIndexAxshList) {
                    productIndexAxsh.setSales(productIndexAxsh.getSales() + cxllProductSalesMap.get(productIndexAxsh.getErpGoodsId()));
                }
                productIndexAxshRepository.save(productIndexAxshList);
            }
            if (!MapUtils.isEmpty(sjejProductSalesMap)) {
                List<ProductIndex> productIndexList = searchService.findByErpGoodsIdIn(new ArrayList<Long>(sjejProductSalesMap.keySet()));
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
