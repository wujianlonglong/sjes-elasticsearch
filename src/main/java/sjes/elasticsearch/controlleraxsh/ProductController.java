package sjes.elasticsearch.controlleraxsh;

import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sjes.elasticsearch.common.CommonMethod;
import sjes.elasticsearch.common.ResponseMessage;
import sjes.elasticsearch.domain.ProductIndex;

import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.opt.NewFlagOpt;
import sjes.elasticsearch.opt.ProductSalesOpt;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;
import sjes.elasticsearch.serviceaxsh.SearchAxshService;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
@RequestMapping("product")
public class ProductController {

    @Autowired
    ProductIndexRepository productIndexRepository;

    @Autowired
    ProductIndexAxshRepository productIndexAxshRepository;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    ProductSalesOpt productSalesOpt;

    @Autowired
    SearchAxshService searchAxshService;


    @RequestMapping(value = "copyProduct", method = RequestMethod.POST)
    @ResponseBody
    public void copyProduct() throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<ProductIndex> productIndices = IteratorUtils.toList(productIndexRepository.findAll().iterator());
        //  productIndexRepository.save(productIndices);
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
//        FacetedPage<ProductIndexAxsh> queryForPage = elasticsearchTemplate.queryForPage(
//                nativeSearchQueryBuilder.withQuery(matchAllQuery()).withPageable(new PageRequest(0, 10000)).withIndices("sjes").withTypes("products").build(), ProductIndexAxsh.class);
//        List<ProductIndexAxsh> productIndices = queryForPage.getContent();
//        List<ItemPrice> itemPriceList=productIndices.get(0).getItemPrices();
        List<ProductIndexAxsh> productIndexAxshList = CommonMethod.listCopy(productIndices, ProductIndexAxsh.class);
//        List<ProductIndexAxsh> productIndexAxshList=new ArrayList<>();
        //  BeanUtils.copyProperties(productIndexAxshList,productIndices);
//        for (ProductIndexAxsh productIndex : productIndices) {
//            ProductIndexAxsh productIndexNew=new ProductIndexAxsh();
//            BeanUtils.copyProperties(productIndexNew,productIndex);
//            productIndexAxshList.add(productIndexNew);
//        }

        productIndexAxshRepository.save(productIndexAxshList);
        System.out.println("成功！");

    }


    /**
     * 手动增量同步商品销售数量
     */
    @RequestMapping(value = "incrSyncSales", method = RequestMethod.POST)
    public void incrSyncSales() {
        productSalesOpt.productSalesIncrSync();
    }


    /**
     * 手动全量同步商品销售数量
     */
    @RequestMapping(value = "allSyncSales", method = RequestMethod.POST)
    public ResponseMessage allSyncSales() {
        return productSalesOpt.productSalesAllSync();
    }


    /**
     * 手动更新商品erp促销活动
     */
//    @RequestMapping(value="updatePromotion",method=RequestMethod.POST)
//    @ResponseBody
//    public ResponseMessage updatePromotion() {
//        return searchAxshService.updatePromotion();//更新商品erp促销信息
//    }


    /**
     * 更新是否为新品标志（上架两周内的为新品）--安鲜生活
     */
    @RequestMapping(value="updateNewFlagAxsh",method=RequestMethod.POST)
    @ResponseBody
    public void updateNewFlagAxsh(){
        searchAxshService.updateNewFlagAxsh();
    }

}



