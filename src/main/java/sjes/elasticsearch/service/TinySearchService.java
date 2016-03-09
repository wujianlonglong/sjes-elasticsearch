package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.Pageable;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.feigns.sale.common.SaleConstant;
import sjes.elasticsearch.feigns.sale.feign.PromotionFeign;
import sjes.elasticsearch.feigns.sale.model.Promotion;
import sjes.elasticsearch.repository.ProductIndexRepository;

import java.util.List;

import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * 用于后台管理的轻量搜索
 *
 * Created by 白 on 2016/3/8.
 */
@Service("tinySearchService")
public class TinySearchService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ProductIndexRepository productIndexRepository;

    @Autowired
    private PromotionFeign promotionFeign;

    /**
     * 获取商品列表
     *
     * @param saleType 促销类型，无则为null
     * @param page 页面
     * @param size 页面大小
     * @return 符合条件的商品（分页）
     */
    public PageModel getProducts(Integer saleType, Integer page, Integer size) {
        Pageable pageable = new Pageable(page, size);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchAllQuery());
        BoolFilterBuilder boolFilterBuilder = boolFilter().must(termFilter("status", 0));

        //过滤掉秒杀商品
        if (saleType != null && saleType == SaleConstant.secondKill) {
            List<Promotion> promotionList = promotionFeign.productIdsForSecondKill(SaleConstant.secondKill);
            promotionList.forEach(promotion -> boolFilterBuilder.mustNot(termFilter("erpGoodsId", promotion.getProductId())));
        }

        nativeSearchQueryBuilder.withFilter(boolFilterBuilder);

        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).build());
        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), pageable);
    }

    /**
     * 根据id或名称获取商品列表
     *
     * @param id 编号
     * @param keyword 名称
     * @param page 页面
     * @param size 页面大小
     * @return 符合条件的商品（分页）
     */
    public PageModel getProductsByIdOrName(Long id, String keyword, Integer page, Integer size) {
        Pageable pageable = new Pageable(page, size);

        if (null == id && StringUtils.isBlank(keyword)) {
            return new PageModel(Lists.newArrayList(), 0, pageable);
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = boolQuery();

        if (StringUtils.isNotBlank(keyword)) {

            if (keyword.matches("[A-Za-z0-9]+")) {
                final String searchKeyword = "*" + keyword.toUpperCase() + "*";
                boolQueryBuilder.must(boolQuery().should(wildcardQuery("namePinYin", searchKeyword))
                        .should(wildcardQuery("namePinYinAddr", searchKeyword)).minimumNumberShouldMatch(1));
            }else{
                boolQueryBuilder.must(boolQuery().should(matchQuery("name", keyword).analyzer("ik")).should(wildcardQuery("name", "*" + keyword + "*")).minimumNumberShouldMatch(1));
            }
        }

        if (null != id) {
            boolQueryBuilder.must(wildcardQuery("goodsIdStr", "*" + id + "*"));
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        nativeSearchQueryBuilder.withFilter(termFilter("status", 0));

        FacetedPage<ProductIndex> facetedPage = productIndexRepository.search(nativeSearchQueryBuilder.withPageable(new PageRequest(pageable.getPage(), pageable.getSize())).build());
        return new PageModel(facetedPage.getContent(), facetedPage.getTotalElements(), pageable);
    }
}