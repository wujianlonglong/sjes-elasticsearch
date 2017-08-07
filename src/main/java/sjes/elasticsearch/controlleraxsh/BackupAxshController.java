package sjes.elasticsearch.controlleraxsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sjes.elasticsearch.common.ServiceException;
import sjes.elasticsearch.serviceaxsh.BackupAxshService;

import java.io.IOException;

/**
 * Created by 白 on 2016/1/7.
 */
@RestController
@RequestMapping("backupaxsh")
public class BackupAxshController {

    @Autowired
    private BackupAxshService backupAxshService;

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public String info() throws ServiceException, IOException {
        return backupAxshService.getSnapshotNameInfo();
    }

    @RequestMapping(value = "backup", method = RequestMethod.GET)
    public String backup() throws ServiceException, IOException {
        return backupAxshService.backup() ? "SUCCESS" : "FAIL";
    }

    @RequestMapping(value = "restore", method = RequestMethod.GET)
    public String restore() throws ServiceException, IOException {
        return backupAxshService.restore() ? "SUCCESS" : "FAIL";
    }

//    @RequestMapping(value = "{snapshot}/backup", method = RequestMethod.GET)
//    public String backup(@PathVariable("snapshot") String snapshot) {
//        return ElasticsearchSnapshotUtils.getSnapshotInfo();
//    }
}
