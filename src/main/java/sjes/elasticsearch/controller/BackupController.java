package sjes.elasticsearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.service.BackupService;

import java.io.IOException;

/**
 * Created by 白 on 2016/1/7.
 */
@RestController
@RequestMapping("backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String test() throws ServiceException, IOException {
        return backupService.backup()+"";
    }
}
