package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.UserActionLogService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:14
 **/


@RestController
@RequestMapping(value = "/useractionlogs")
public class UserActionLogController {

    private final UserActionLogService userActionLogService;

    @Autowired
    public UserActionLogController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    // 查询全部用户操作日志
    @GetMapping
    public ResultEntity getUserActionLogs(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(userActionLogService.getUserActionLogs(pageable));
    }
}
