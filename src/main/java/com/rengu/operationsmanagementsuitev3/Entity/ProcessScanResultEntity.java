package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-08-31 21:48
 **/

@Data
public class ProcessScanResultEntity implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String pid;
    private String name;
    private int priority;
    private double ramUsedSize;
}
