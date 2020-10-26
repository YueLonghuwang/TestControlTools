package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignScanResultService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-10 16:06
 **/

@RestController
@RequestMapping(value = "/deploymentdesignscanresults")
public class DeploymentDesignScanResultController {

    private final DeploymentDesignScanResultService deploymentDesignScanResultService;

    @Autowired
    public DeploymentDesignScanResultController(DeploymentDesignScanResultService deploymentDesignScanResultService) {
        this.deploymentDesignScanResultService = deploymentDesignScanResultService;
    }

    @GetMapping
    public ResultEntity getDeploymentDesignScanResultsByOrderId(@RequestParam(value = "orderId") String orderId) {
        return ResultUtils.build(deploymentDesignScanResultService.getDeploymentDesignScanResultsByOrderId(orderId));
    }
}
