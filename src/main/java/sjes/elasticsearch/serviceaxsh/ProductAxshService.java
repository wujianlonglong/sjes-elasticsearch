package sjes.elasticsearch.serviceaxsh;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.feigns.item.feignaxsh.ProductAxshFeign;
import sjes.elasticsearch.feigns.item.model.HomeCategoryRelation;
import sjes.elasticsearch.feigns.item.model.ProductImageModel;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.utils.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qinhailong on 15-12-4.
 */
@Service("productAxshService")
public class ProductAxshService {

    private static Logger log = LoggerFactory.getLogger(ProductAxshService.class);

    @Autowired
    private ProductAxshFeign productAxshFeign;

    @Autowired
    ProductIndexAxshRepository productIndexAxshRepository;

    /**
     * 根据productId得到指定的ProductImageModel
     *
     * @param productId 商品id
     * @return ProductImageModel
     */
    public ProductImageModel getProductImageModel(Long productId) {
        return productAxshFeign.getProductImageModel(productId);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param productIds 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listProductsImageModel(List<Long> productIds) {
        return productAxshFeign.listProductsImageModel(productIds);
    }

    /**
     * 根据商品id列表查询 ProductsImageModel列表
     *
     * @param sns 商品id列表
     * @return ProductsImageModel列表
     */
    public List<ProductImageModel> listBySns(List<String> sns) {
        return productAxshFeign.listBySns(sns);
    }

    /**
     * 根据分类Ids查询商品列表
     *
     * @param categoryIds 分类Ids
     * @return 商品列表
     */
    public List<ProductImageModel> listByCategoryIds(List<Long> categoryIds) {
        List<ProductImageModel> productImageModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productImageModels.addAll(productAxshFeign.listByCategoryIds(cateIds));
            }
        }
        return productImageModels;
    }

    public List<ProductImageModel> listByCategoryIdsnew(List<Long> categoryIds) {
        List<ProductImageModel> productImageModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productImageModels.addAll(productAxshFeign.listByCategoryIdsnew(cateIds));
            }
        }
        return productImageModels;
    }


    public List<ProductImageModel> listByCategoryIdsCateNum(String shopId,List<Long> categoryIds) {
        List<ProductImageModel> productImageModels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            List<List<Long>> categoryIdsList = ListUtils.splitList(categoryIds, ListUtils.SPLIT_SUB_LIST_SIZE);
            for (List<Long> cateIds : categoryIdsList) {
                productImageModels.addAll(productAxshFeign.listByCategoryIdsCateNum(shopId,cateIds));
            }
        }
        return productImageModels;
    }


    public void allHomeCategorySync() {
        try {
            List<HomeCategoryRelation> homeCategoryRelations = productAxshFeign.findAllHomeCategoryRelation();
            if (CollectionUtils.isEmpty(homeCategoryRelations)) {
                log.error("未查询到首页商品分类数据！");
                return;
            }
            Map<Long, List<String>> map = new HashMap<>();
            for (HomeCategoryRelation homeCategoryRelation : homeCategoryRelations) {
                Long erpGoodsId = Long.valueOf(homeCategoryRelation.getErpGoodsId());
                String homeCategoryId = homeCategoryRelation.getHomeCategoryId();
                if (map.containsKey(erpGoodsId)) {
                    List<String> categoryList = map.get(erpGoodsId);
                    categoryList.add(homeCategoryId);
                } else {
                    List<String> categoryList = new ArrayList<>();
                    categoryList.add(homeCategoryId);
                    map.put(erpGoodsId, categoryList);
                }
            }

            List<Long> erpGoodsIds = new ArrayList(map.keySet());
            int length = erpGoodsIds.size();
            int batchNum = 1000;
            int roop = (length + batchNum - 1) / batchNum;
            Pageable pageable = new PageRequest(0, batchNum);
            List<ProductIndexAxsh> productIndexAxshList = new ArrayList<>();
            for (int i = 0; i < roop; i++) {
                int startIndex=i * batchNum;
                int endIndex=(i + 1) * batchNum>length?length:(i + 1) * batchNum;
                List<Long> subList = erpGoodsIds.subList(startIndex, endIndex);
                List<ProductIndexAxsh>  productIndexAxshes = productIndexAxshRepository.findByErpGoodsIdIn(subList, pageable).getContent();
                productIndexAxshList.addAll(productIndexAxshes);
            }

            for (ProductIndexAxsh productIndexAxsh : productIndexAxshList) {
                Long erpGoodsId = productIndexAxsh.getErpGoodsId();
                List<String> homeCategoryIds = map.get(erpGoodsId);
                productIndexAxsh.setHomeCategoryIds(homeCategoryIds);
            }
            productIndexAxshRepository.save(productIndexAxshList);
        } catch (Exception ex) {
            log.error("全量同步首页商品分类数据失败："+ex.toString());
        }

    }

    public void initAllHomeCategorySync() {
        try {
            List<ProductIndexAxsh> allProducts= IteratorUtils.toList(productIndexAxshRepository.findAll().iterator());
            List<HomeCategoryRelation> homeCategoryRelations = productAxshFeign.findAllHomeCategoryRelation();
            if (CollectionUtils.isEmpty(homeCategoryRelations)) {
                log.error("未查询到首页商品分类数据！");
                return;
            }
            Map<Long, List<String>> map = new HashMap<>();
            for (HomeCategoryRelation homeCategoryRelation : homeCategoryRelations) {
                Long erpGoodsId = Long.valueOf(homeCategoryRelation.getErpGoodsId());
                String homeCategoryId = homeCategoryRelation.getHomeCategoryId();
                if (map.containsKey(erpGoodsId)) {
                    List<String> categoryList = map.get(erpGoodsId);
                    categoryList.add(homeCategoryId);
                } else {
                    List<String> categoryList = new ArrayList<>();
                    categoryList.add(homeCategoryId);
                    map.put(erpGoodsId, categoryList);
                }
            }

            for (ProductIndexAxsh allProduct : allProducts) {
                Long erpGoodsId=allProduct.getErpGoodsId();
                if(!map.containsKey(erpGoodsId)){
                    allProduct.setHomeCategoryIds(null);
                }else{
                    allProduct.setHomeCategoryIds(map.get(erpGoodsId));
                }
            }

            productIndexAxshRepository.save(allProducts);

        } catch (Exception ex) {
            log.error("初始化全量同步首页商品分类数据失败："+ex.toString());
        }

    }
}
