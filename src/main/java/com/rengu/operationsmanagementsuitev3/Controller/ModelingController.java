package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ModelingEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ModelingService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * TODO
 *
 * @author yyc
 * @version 1.0
 * @date 2020/10/26 11:15
 */
@RestController
@RequestMapping(value = "/modeling")
public class ModelingController {

    private final ModelingService modelingService;

    public ModelingController(ModelingService modelingService) {
        this.modelingService = modelingService;
    }

    //分页查询导出体系建模设计方案
    @GetMapping(value = "/findAllModelingByPage")
    public ResultEntity findAllModeling(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(modelingService.findAllModeling(pageable));
    }

    //分页查询导出体系建模设计方案
    @PostMapping(value = "/saveModeling")
    public ResultEntity saveModeling(@RequestBody List<ModelingEntity> list) {
        return ResultUtils.build(modelingService.saveModeling(list));
    }

}
