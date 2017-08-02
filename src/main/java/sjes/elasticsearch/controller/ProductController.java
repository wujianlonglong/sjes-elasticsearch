package sjes.elasticsearch.controller;

import com.netflix.discovery.converters.Auto;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.IteratorUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sjes.elasticsearch.common.CommonMethod;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.domain.ProductIndexNew;
import sjes.elasticsearch.feigns.item.model.ItemPrice;
import sjes.elasticsearch.repository.ProductIndexNewRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.filter;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@Controller
@RequestMapping("product")
public class ProductController {

    @Autowired
    ProductIndexRepository productIndexRepository;

    @Autowired
    ProductIndexNewRepository productIndexNewRepository;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping(value = "copyProduct", method = RequestMethod.POST)
    @ResponseBody
    public void copyProduct() throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<ProductIndex> productIndices = IteratorUtils.toList(productIndexRepository.findAll().iterator());
      //  productIndexRepository.save(productIndices);
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
//        FacetedPage<ProductIndex> queryForPage = elasticsearchTemplate.queryForPage(
//                nativeSearchQueryBuilder.withQuery(matchAllQuery()).withPageable(new PageRequest(0, 10000)).withIndices("sjes").withTypes("products").build(), ProductIndex.class);
//        List<ProductIndex> productIndices = queryForPage.getContent();
//        List<ItemPrice> itemPriceList=productIndices.get(0).getItemPrices();
        List<ProductIndexNew> productIndexNewList = CommonMethod.listCopy(productIndices, ProductIndexNew.class);
//        List<ProductIndexNew> productIndexNewList=new ArrayList<>();
      //  BeanUtils.copyProperties(productIndexNewList,productIndices);
//        for (ProductIndex productIndex : productIndices) {
//            ProductIndexNew productIndexNew=new ProductIndexNew();
//            BeanUtils.copyProperties(productIndexNew,productIndex);
//            productIndexNewList.add(productIndexNew);
//        }

        productIndexNewRepository.save(productIndexNewList);
        System.out.println("成功！");





    }

}
