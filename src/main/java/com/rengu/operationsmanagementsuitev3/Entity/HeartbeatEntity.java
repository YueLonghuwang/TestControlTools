package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 13:50
 **/

@Data
public class HeartbeatEntity implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String hostAddress;
    private String cpuTag;
    private long cpuClock;
    private int cpuUtilization;
    private double ramFreeSize;
    private double ramTotalSize;
    private double downLoadSpeed;
    private double upLoadSpeed;
    private int OSType;
    private String OSName;
    private int count = 3;
}
