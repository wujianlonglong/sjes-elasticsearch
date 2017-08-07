package sjes.elasticsearch.controlleraxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.feigns.sale.common.SaleConstant;
import sjes.elasticsearch.serviceaxsh.TinySearchAxshService;

/**
 * Created by 白 on 2016/3/8.
 *
 * 用于后台管理的轻量搜索
 */
@RestController
@RequestMapping("tinysearchsaxsh")
public class TinySearchAxshController {

    @Autowired
    private TinySearchAxshService tinySearchAxshService;

    /**
     * 获取所有正常销售的商品
     */
    @RequestMapping(value = "all", method = RequestMethod.GET)
    public PageModel getAllProducts(Integer page, Integer size) throws ServiceException {
        return tinySearchAxshService.getProducts(null,null,null, page, size);
    }

    /**
     * 获取不参与秒杀的商品
     */
    @RequestMapping(value = "nopromotion", method = RequestMethod.GET)
    public PageModel getProductsWithoutPromotion(Integer page, Integer size) throws ServiceException {
        return tinySearchAxshService.getProducts(null, null, SaleConstant.secondKill, page, size);
    }

    /**
     * 根据相关条件获取商品
     *
     * @param id 编号
     * @param keyword 关键字
     * @param saleType 促销类型
     * @param page 页面
     * @param size 页面大小
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "searchs", method = RequestMethod.GET)
    public PageModel getProductsByIdOrName(@RequestParam(name = "id",required = false) Long id, @RequestParam(name = "keyword",required = false)String keyword, @RequestParam(name = "saleType",required = false)Integer saleType, @RequestParam("page")Integer page,@RequestParam("size") Integer size) throws ServiceException {
        return tinySearchAxshService.getProducts(id, keyword, saleType, page, size);
    }

    /**
     * 根据相关条件获取商品
     *
     * @param id 编号
     * @param keyword 关键字
     * @param page 页面
     * @param size 页面大小
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "listProducts", method = RequestMethod.GET)
    public PageModel listProducts(Long id, String keyword, Integer page, Integer size) {
        return tinySearchAxshService.listProducts(id, keyword, page, size);
    }
}
