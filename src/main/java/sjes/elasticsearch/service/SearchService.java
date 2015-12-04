package sjes.elasticsearch.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.CategoryIndex;
import sjes.elasticsearch.domain.Product;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.repository.CategoryIndexModelRepository;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

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
                // TODO


            }
        } catch (Exception e) {
            LOGGER.error("初始化索引出现错误！", e);
            throw new ServiceException("初始化索引出现错误！", e.getCause());
        }
    }

    /**
     * 建立索引
     * @throws ServiceException
     */
    public void index() throws ServiceException {

        CategoryIndex categoryIndex = new CategoryIndex();

        List<ProductIndex> productIndexList = Lists.newArrayList();

        ProductIndex productIndex = new ProductIndex();
        productIndex.setName("康师傅红烧牛肉面");
        productIndex.setBrandName("康师傅");

        productIndexList.add(productIndex);
        //categoryIndex.setId("12345");
        categoryIndex.setProductIndexes(productIndexList);

        categoryIndexModelRepository.save(categoryIndex);
    }

    /**
     * 删除索引
     * @throws ServiceException
     */
    public void deleteIndex() throws ServiceException {

        categoryIndexModelRepository.delete("12345");

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
    public String searchProduct(String name) throws ServiceException {

        //BoolQueryBuilder builder = boolQuery().must(QueryBuilders.matchQuery("products.name", name));

        QueryBuilder builder = nestedQuery("products", boolQuery().must(matchQuery("name", name)));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build();

        List<CategoryIndex> cate = categoryIndexModelRepository.search(searchQuery).getContent();
        if(cate.size() > 0) {
            return cate.get(0).getProductIndexes().get(0).getName();


        }else{
            return "not found";
        }
    }
}
