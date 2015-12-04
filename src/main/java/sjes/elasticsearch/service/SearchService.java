package sjes.elasticsearch.service;

import com.oracle.tools.packager.Log;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.*;
import sjes.elasticsearch.repository.CategoryIndexModelRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qinhailong on 15-12-2.
 */
@Service("searchService")
public class SearchService {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private CategoryIndexModelRepository categoryIndexModelRepository;

    @Autowired
    private CategoryService categoryService;

    /**
     * 初始化索引
     */
    public void initService() throws ServiceException {
        LOGGER.debug("开始初始化索引！");
        try {
            List<CategoryIndex> categoryIndexes = categoryService.getCategoryIndexs();
            if (CollectionUtils.isNotEmpty(categoryIndexes)) {

                for (CategoryIndex categoryIndex : categoryIndexes) {
                    categoryIndexModelRepository.save(categoryIndex);
                }
           }
        } catch (Exception e) {
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
        }
    }

    /**
     * 建立单个索引
     * @throws ServiceException
     */
    public void index(CategoryIndex categoryIndex) throws ServiceException {
        categoryIndexModelRepository.save(categoryIndex);
    }

    /**
     * 建立索引(临时测试，用完删)
     * @throws ServiceException
     */
    public void testIndex() throws ServiceException {
        LOGGER.info("start index");
        CategoryIndex categoryIndex = new CategoryIndex();
        ProductIndex productIndex1 = new ProductIndex();
        productIndex1.setName("康师傅红烧牛肉面");
        ProductIndex productIndex2 = new ProductIndex();
        productIndex2.setName("统一老坛酸菜牛肉面");
        ProductIndex productIndex3 = new ProductIndex();
        productIndex3.setName("康师傅海鲜牛肉面");
        List<ProductIndex> list = new ArrayList<>();
        list.add(productIndex1);
        list.add(productIndex2);
        list.add(productIndex3);
        categoryIndex.setProductIndexes(list);

        categoryIndexModelRepository.save(categoryIndex);

        LOGGER.info("stop index" + categoryIndexModelRepository.count());
    }

    /**
     * 删除全部索引
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {
        categoryIndexModelRepository.deleteAll();
    }

    /**
     * 删除指定索引
     * @throws ServiceException
     */
    public void deleteIndex(String categoryId) throws ServiceException {

        categoryIndexModelRepository.delete(categoryId);

    }

    /**
     * 删除指定索引
     * @throws ServiceException
     */
    public void deleteIndex(CategoryIndex categoryIndex) throws ServiceException {

        categoryIndexModelRepository.delete(categoryIndex);

    }

    /**
     * 查询
     * @return
     * @throws ServiceException
     */
    public Object search() throws ServiceException {
        return null;
    }

    /**
     * 分类查询
     * @return
     * @throws ServiceException
     */
    public Object searchCategory()throws ServiceException  {
        return null;
    }

    /**
     * 产品查询
     * @return
     * @throws ServiceException
     */
//    public String searchProduct(String name) throws ServiceException {
//
//        //BoolQueryBuilder builder = boolQuery().must(QueryBuilders.matchQuery("products.name", name));
//
//        QueryBuilder builder = nestedQuery("products", boolQuery().must(matchQuery("name", name)));
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build();
//
//        List<CategoryIndex> cate = categoryIndexModelRepository.search(searchQuery).getContent();
//        if(cate.size() > 0) {
//            return cate.get(0).getProductIndexes().get(0).getName();
//
//
//        }else{
//            return "not found";
//        }
//    }
}
