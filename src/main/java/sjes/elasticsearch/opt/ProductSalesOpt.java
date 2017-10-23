package sjes.elasticsearch.opt;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.feigns.order.feign.SellFeign;
import sjes.elasticsearch.feigns.order.model.ProductSales;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.service.SearchService;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSalesOpt {

    private static Logger log = LoggerFactory.getLogger(ProductSalesOpt.class);



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


    /**
     * 自动增量同步商品销售量
     */
    @Scheduled(cron = "0 */30 7-19 * * ?")
    public void productSalesIncrSync() {
        log.info("增量同步商品销量开始-------" + LocalDateTime.now());
        int syncType = 0;
        try {
            ProductSalesSyncs(syncType);
        } catch (Exception ex) {
            log.error("增量同步商品销量失败：" + ex.toString());
            return;
        } finally {
            log.info("增量同步商品销售结束-------" + LocalDateTime.now());
        }

    }


    /**
     * 自动全量同步商品销售量(暂时不启用)
     */
    // @Scheduled(cron="0 0 1 * * ?")
    public ResponseMessage productSalesAllSync() {
        log.info("全量同步商品销量开始-------" + LocalDateTime.now());
        int syncType = 1;
        try {
            refreshSales();
            refreshSalesAxsh();
            ProductSalesSyncs(syncType);
            return ResponseMessage.success("全量同步商品销售成功！");
        } catch (Exception ex) {
            log.error("全量同步商品销量失败：" + ex.toString());
            return ResponseMessage.error("全量同步商品销量失败：" + ex.toString());
        } finally {
            log.info("全量同步商品销售结束-------" + LocalDateTime.now());
        }

    }

    /**
     * 将所有Axsh商品销量置0
     */
    private void refreshSalesAxsh() {
        List<ProductIndexAxsh> productIndexAxshes = IteratorUtils.toList(productIndexAxshRepository.findAll().iterator());
        for (ProductIndexAxsh productIndexAxsh : productIndexAxshes) {
            //先清空所有的销售量
            productIndexAxsh.setSales(0L);
        }
        productIndexAxshRepository.save(productIndexAxshes);
    }

    /**
     * 将所有网购商品销量置0
     */
    private void refreshSales() {
        List<ProductIndex> productIndexList = IteratorUtils.toList(productIndexRepository.findAll().iterator());
        for (ProductIndex productIndex : productIndexList) {
            //先清空所有的销售量
            productIndex.setSales(0L);
        }
        productIndexRepository.save(productIndexList);
    }

    /**
     * 同步商品销量
     *
     * @param syncType 同步类型：0或空---增量同步；1---全量同步
     */
    public void ProductSalesSyncs(int syncType) {
        synchronized (ProductSalesOpt.class) {
            List<ProductSales> productSalesList = sellFeign.getSellList(syncType);
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

        }
    }

}
