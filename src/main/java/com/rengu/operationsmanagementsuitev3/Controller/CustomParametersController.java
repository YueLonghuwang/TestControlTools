package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.CustomParametersEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: zhangqiankun
 * Date: 2020/8/10 9:09
 */
@RestController
@RequestMapping(value = "CustomParameters")
public class CustomParametersController {

   /* //保存自定义参数
    @PostMapping(value = "{prId}/CustomParameters")
    public ResultEntity saveCustomParameters(@PathVariable(value = "prId")String prId){
        return ResultUtils.build(customParametersService.saveCustomParameters(prId));
    }*/

}
