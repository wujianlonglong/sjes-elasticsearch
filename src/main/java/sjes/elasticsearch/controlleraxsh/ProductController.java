package sjes.elasticsearch.controlleraxsh;

import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sjes.elasticsearch.common.CommonMethod;
import sjes.elasticsearch.domain.ProductIndex;

import sjes.elasticsearch.domainaxsh.ProductIndexAxsh;
import sjes.elasticsearch.repositoryaxsh.ProductIndexAxshRepository;
import sjes.elasticsearch.repository.ProductIndexRepository;

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

}
