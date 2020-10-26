package com.rengu.operationsmanagementsuitev3.Utils;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultString;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:11
 **/

@Slf4j
public class ResultUtils {

    public static ResultEntity<Object> build(Object data) {
        ResultEntity<Object> resultEntity = new ResultEntity<>();
        resultEntity.setData(data);
        return resultEntity;
    }

    //返回字符串
    public static String builds(String data) {
        ResultString resultString = new ResultString();
        resultString.setData(data);
        return resultString.getData();
    }
}
