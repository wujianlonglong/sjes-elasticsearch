package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.domain.HotWordModel;
import sjes.elasticsearch.domain.PageModel;
import sjes.elasticsearch.domain.ProductIndex;
import sjes.elasticsearch.feigns.sale.common.SaleConstant;
import sjes.elasticsearch.feigns.sale.model.Promotion;
import sjes.elasticsearch.service.TinySearchService;

import java.util.List;

/**
 * Created by 白 on 2016/3/8.
 *
 * 用于后台管理的轻量搜索
 */
@RestController
@RequestMapping("tinysearchs")
public class TinySearchController {

    @Autowired
    private TinySearchService tinySearchService;

    /**
     * 获取所有正常销售的商品
     */
    @RequestMapping(value = "all", method = RequestMethod.GET)
    public PageModel getAllProducts(Integer page, Integer size) throws ServiceException {
        return tinySearchService.getProducts(null, page, size);
    }

    /**
     * 获取不参与秒杀的商品
     */
    @RequestMapping(value = "nopromotion", method = RequestMethod.GET)
    public PageModel getProductsWithoutPromotion(Integer page, Integer size) throws ServiceException {
        return tinySearchService.getProducts(SaleConstant.secondKill, page, size);
    }

    /**
     * 根据Id或名称获取商品
     */
    @RequestMapping(value = "searchs", method = RequestMethod.GET)
    public PageModel getProductsByIdOrName(Long id, String keyword, Integer page, Integer size) throws ServiceException {
        return tinySearchService.getProductsByIdOrName(id,keyword, page, size);
    }
}
