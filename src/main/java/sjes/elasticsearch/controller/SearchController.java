package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.service.SearchService;

/**
 * Created by qinhailong on 15-12-2.
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @RequestMapping("/index")
    public String index() {

        try {
            searchService.index();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        return "index";
    }

    @RequestMapping("/delete")
    public String deleteIndex() {

        try {
            searchService.deleteIndex();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        return "delete";
    }

    @RequestMapping("/search")
    public String search(@RequestParam(value = "name") String name) {

        String result = null;
        try {
            result = searchService.searchProduct(name);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        return result;
    }
}
